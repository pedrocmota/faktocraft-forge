import json
import os
import re
import shutil
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
SRC = os.path.join(ROOT, 'src/main/java/com/faktocraft/gametest')
RES = os.path.join(ROOT, 'src/main/resources/data/faktocraft')
INSTANCES = os.path.join(RES, 'test_instance')
ENVS = os.path.join(RES, 'test_environment')

ANNOTATION = re.compile(r'@GameTest\s*\(([^)]*)\)\s*\n\s*(?:public\s+)?(static\s+)?void\s+(\w+)\s*\(', re.M)
CONST = re.compile(r'(?:(?:private|protected|public)\s+)?static\s+final\s+String\s+(\w+)\s*=\s*"([^"]+)"')


def parse_args(text):
    args = {}
    for part in re.split(r',(?![^"]*"[^"]*(?:"[^"]*"[^"]*)*$)', text):
        part = part.strip()
        if not part:
            continue
        key, _, value = part.partition('=')
        args[key.strip()] = value.strip()
    return args


def main():
    if os.path.isdir(INSTANCES):
        shutil.rmtree(INSTANCES)
    os.makedirs(INSTANCES)
    os.makedirs(ENVS, exist_ok=True)
    entries = []
    batches = set()
    for dirpath, _, files in os.walk(SRC):
        for name in sorted(files):
            if not name.endswith('.java'):
                continue
            path = os.path.join(dirpath, name)
            text = open(path, encoding='utf-8').read()
            consts = dict(CONST.findall(text))
            pkg = re.search(r'^package\s+([\w.]+);', text, re.M).group(1)
            cls = name[:-5]
            for match in ANNOTATION.finditer(text):
                args = parse_args(match.group(1))
                method = match.group(3)
                static = match.group(2) is not None
                template = args.get('template', '""').strip('"')
                if template in consts:
                    template = consts[template]
                timeout = int(args.get('timeoutTicks', '100'))
                setup = int(args.get('setupTicks', '0'))
                required = args.get('required', 'true') == 'true'
                batch = args.get('batch', '"defaultBatch"').strip('"')
                env = 'minecraft:default'
                if batch != 'defaultBatch':
                    env_name = re.sub(r'([a-z])([A-Z])', r'\1_\2', batch).lower()
                    env = 'faktocraft:' + env_name
                    batches.add(env_name)
                test_id = cls.lower() + '.' + method.lower()
                data = {
                    'type': 'minecraft:function',
                    'function': 'faktocraft:' + test_id,
                    'environment': env,
                    'structure': 'faktocraft:' + template,
                    'max_ticks': timeout,
                    'sky_access': True,
                }
                if setup:
                    data['setup_ticks'] = setup
                if not required:
                    data['required'] = False
                with open(os.path.join(INSTANCES, test_id + '.json'), 'w', encoding='utf-8', newline='\n') as out:
                    json.dump(data, out, indent=2)
                    out.write('\n')
                entries.append((pkg + '.' + cls, method, test_id, static))
    for batch in sorted(batches):
        with open(os.path.join(ENVS, batch + '.json'), 'w', encoding='utf-8', newline='\n') as out:
            json.dump({'type': 'minecraft:all_of', 'definitions': []}, out, indent=2)
            out.write('\n')
    entries.sort(key=lambda e: e[2])
    lines = ['package com.faktocraft.gametest;', '',
             'final class GameTestIndex {', '', '  private GameTestIndex() {', '  }', '',
             '  static final String[][] TESTS = {']
    for cls, method, test_id, static in entries:
        lines.append('      { "%s",' % cls)
        lines.append('          "%s",' % method)
        lines.append('          "%s", "%s" },' % (test_id, 'static' if static else 'instance'))
    lines += ['  };', '}', '']
    with open(os.path.join(SRC, 'GameTestIndex.java'), 'w', encoding='utf-8', newline='\n') as out:
        out.write('\n'.join(lines))
    print('tests:', len(entries), 'environments:', sorted(batches))


if __name__ == '__main__':
    main()
