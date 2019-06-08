#! /bin/python3
import json, sys

def generate_toString(typ, name, literal, flat):
    typ_name = typ
    sep = ''
    if type(typ) is dict:
        typ_name = typ['type']
        if 'bigPrint' in typ and typ['bigPrint'] == True:
            sep = '\\n'

    to_string = 'toString()'

    if flat == True:
        sep = ''
        to_string = 'toFlatString()'


    if 'List' in typ_name:
        return f"\"[{sep}\" + this.{name}.stream().map(e -> e.{to_string}).collect(Collectors.joining(\", {sep}\")) + \" {sep}]\""
    if literal:
        return f"\"\\\"\" + this.{name}.toString() + \"\\\"\""
    if 'String' in typ_name or 'Double' in typ_name or 'Op' in typ_name or 'Integer' in typ_name:
        return f"this.{name}.toString()"
    return f"this.{name}.{to_string}"

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
    ctr_params = []
    java_fields = []
    ctr_assign = []

    (extends, super_init, ignores) = generate_extends(spec)

    for name, typ in raw_fields.items():
        if type(typ) is dict:
            fields[name] = typ['type']
            if 'lazy' in typ and typ['lazy'] == True:
                if name not in ignores:
                    java_fields.append('public ' + typ['type'] + ' ' + name + ';')
                    ctr_assign.append(f"this.{name} = null;")
            else:
                ctr_params.append(typ['type'] + ' ' + name)
                if name not in ignores:
                    java_fields.append('public final ' + typ['type'] + ' ' + name + ';')
                    ctr_assign.append(f"this.{name} = {name};")
        else:
            fields[name] = typ
            ctr_params.append(typ + ' ' + name)
            if name not in ignores:
                java_fields.append('public final ' + typ + ' ' + name + ';')
                ctr_assign.append(f"this.{name} = {name};")


    literal = False
    if 'literal' in spec and spec['literal'] == True:
        literal = True

    ctr_params = ', '.join(ctr_params)
    to_string_values = ', " + \n            "'.join([f"{name} = \" + {generate_toString(typ, name, literal, False)} + \"" for name, typ in raw_fields.items()])
    to_string_flat_values = ', " + \n            "'.join([f"{name} = \" + {generate_toString(typ, name, literal, True)} + \"" for name, typ in raw_fields.items()])

    java_fields = '\n    '.join(java_fields)
    ctr_assign = '\n        '.join(ctr_assign)

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

    public String toFlatString() {cbo}
        return "{class_name}({to_string_flat_values})";
    {cbc}

    public CompiledCode compile(ASTVisitor visitor, CompileContext context) {cbo}
        return visitor.compile{class_name}(this, context);
    {cbc}
{cbc}
    """, f"""
public CompiledCode compile{class_name}({class_name} stmt, CompileContext context) {cbo} 
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


