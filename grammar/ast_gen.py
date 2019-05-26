#! /bin/python3
import json, sys

def generate_toString(typ, name, literal):
    typ_name = typ
    sep = ''
    if type(typ) is dict:
        typ_name = typ['name']
        sep = '\\n'

    if 'List' in typ_name:
        return f"\"[{sep}\" + this.{name}.stream().map(Objects::toString).collect(Collectors.joining(\", {sep}\")) + \" {sep}]\""
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

    raw_fields = spec['fields']

    fields = {}

    for name, typ in raw_fields.items():
        if type(typ) is dict:
            fields[name] = typ['name']
        else:
            fields[name] = typ

    (extends, super_init, ignores) = generate_extends(spec)

    literal = False
    if 'literal' in spec and spec['literal'] == True:
        literal = True

    ctr_params = ', '.join([typ + ' ' + name for name, typ in fields.items()])
    to_string_values = ', " + \n            "'.join([f"{name} = \" + {generate_toString(typ, name, literal)} + \"" for name, typ in raw_fields.items()])

    for ignore in ignores:
        del fields[ignore]

    java_fields = '\n    '.join(['public final ' + typ + ' ' + name + ';' for name, typ in fields.items()])
    ctr_assign = '\n        '.join([f"this.{name} = {name};" for name, typ in fields.items()])

    return (f"""
public static class {class_name} {extends} {cbo}
    {java_fields}
    public {class_name}({ctr_params}) {cbo}
        {super_init}
        {ctr_assign}
    {cbc}
    public String toString() {cbo}
        return "{class_name}({to_string_values})";
    {cbc}

    public CompiledCode compile(ASTVisitor visitor) {cbo}
        return visitor.compile{class_name}(this);
    {cbc}
{cbc}
    """, f"""
public CompiledCode compile{class_name}({class_name} stmt) {cbo} 
    return null; 
{cbc}
    """
    )

data = {}

with open('grammar.json', 'r') as f:
    data = json.load(f)

with open('ast.java', 'w+') as ast:
    with open('visitor.java', 'w+') as visit:
        for key, value in data.items():
            (ast_code, visitor_code) = generate_def(key, value) 
            ast.write(ast_code)
            visit.write(visitor_code)


