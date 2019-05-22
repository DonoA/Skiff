#! /bin/python3
import json, sys

def generate_toString(typ, name, literal):
    if 'List' in typ:
        return f"\"[ \\n\" + this.{name}.stream().map(Objects::toString).collect(Collectors.joining(\", \\n\")) + \" \\n]\""
    if literal:
        return f"\"\\\"\" + this.{name}.toString() + \"\\\"\""
    return f"this.{name}.toString()"

def generate_extends(spec):
    if 'extends' not in spec:
        return ('', '', [])
    arr = spec['extends'].split('(')
    extends = 'extends ' + arr[0]
    if len(arr) != 2:
        arr.append(')')
    super_init = 'super(' + arr[1] + ';'
    ignores = []
    if len(arr[1]) > 1:
        ignores = arr[1][0:-1].split(',')
    return (extends, super_init, ignores)

def generate_def(class_name, spec):
    cbo = "{"
    cbc = "}"

    fields = spec['fields']

    (extends, super_init, ignores) = generate_extends(spec)

    literal = False
    if 'literal' in spec and spec['literal'] == True:
        literal = True

    ctr_params = ', '.join([typ + ' ' + name for name, typ in fields.items()])
    to_string_values = ', " + \n            "'.join([f"{name} = \" + {generate_toString(typ, name, literal)} + \"" for name, typ in fields.items()])

    for ignore in ignores:
        del fields[ignore]

    java_fields = '\n    '.join(['public final ' + typ + ' ' + name + ';' for name, typ in fields.items()])
    ctr_assign = '\n        '.join([f"this.{name} = {name};" for name, typ in fields.items()])

    return f"""
public static class {class_name} {extends} {cbo}
    {java_fields}
    public {class_name}({ctr_params}) {cbo}
        {super_init}
        {ctr_assign}
    {cbc}
    public String toString() {cbo}
        return "{class_name}({to_string_values})";
    {cbc}
{cbc}
    """

    sys.exit(0)

data = {}

with open('grammar.json', 'r') as f:
    data = json.load(f)

for key, value in data.items():
    print(generate_def(key, value))