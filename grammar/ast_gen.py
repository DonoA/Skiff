#! /bin/python3
import json
import sys

cbo = '{'
cbc = '}'

class ASTField:
    def __init__(self, name, typ, big_print, lazy, is_list, no_flat_string, default):
        self.name = name
        self.typ = typ
        self.big_print = big_print
        self.lazy = lazy
        self.is_list = is_list
        self.no_flat_string = no_flat_string
        self.default = default

class ASTClass:
    def __init__(self, name, extends, literal, fields):
        self.name = name
        self.extends = extends
        self.literal = literal
        self.fields = fields
        self.super_fields = []

def comile_class(name, spec):
    extends = None
    if 'extends' in spec:
        extends = spec['extends']
    
    literal = False
    if 'literal' in spec:
        literal = spec['literal']
    
    field_decs = []
    fields = []
    if 'fields' in spec:
        field_decs = spec['fields']

    for fname, field in field_decs.items():
        big_print = False
        lazy = False
        typ = field
        no_flat_string = False
        default = None 

        if type(field) is dict:
            if 'bigPrint' in field:
                big_print = field['bigPrint']

            if 'lazy' in field:
                lazy = field['lazy']

            if 'default' in field:
                default = field['default']
                if not lazy:
                    print(f'ERROR: Field {fname} with a default must be lazy!')
                    sys.exit(1)

            if 'noFlatString' in field:
                no_flat_string = field['noFlatString']

            typ = field['type']

        is_list = 'List' in typ
        no_flat_string = no_flat_string or 'String' in typ

        fields.append(ASTField(
            fname, typ, big_print, lazy, is_list, no_flat_string, default
        ))
    
    return ASTClass(name, extends, literal, fields)

def get_parent_fields(all_classes, clazz):
    if clazz.extends is None:
        return []
    return all_classes[clazz.extends].fields + \
        get_parent_fields(all_classes, all_classes[clazz.extends])

def generate_toString(field, clazz):
    return _generate_to_string(field, clazz, False)

def generate_toFlatString(field, clazz):
    return _generate_to_string(field, clazz, True)

def _generate_to_string(field, clazz, flat):
    name = field.name

    sep = ''
    if field.big_print and not flat:
        sep = '\\n'

    to_string = 'toString()'
    if flat and not field.no_flat_string:
        to_string = 'toFlatString()'

    string_construction = f"this.{name}.{to_string}"

    if field.is_list:
        string_construction = f"\"[{sep}\" + this.{name}.stream().map(e -> e.{to_string}).collect(Collectors.joining(\", {sep}\")) + \" {sep}]\""
    elif clazz.literal:
        string_construction = f"\"\\\"\" + this.{name}.toString() + \"\\\"\""
    
    return f"{name} = \" + {string_construction} + \""

def generate_def(class_name, clazz):
    all_class_fields = clazz.fields + clazz.super_fields

    ctr_params = [f'{f.typ} {f.name}' for f in filter(lambda x: not x.lazy, all_class_fields)]
    java_fields = []

    for f in clazz.fields:
        final = 'final'
        if f.lazy:
            final = ''
        java_fields.append(f'        public {final} {f.typ} {f.name};')
    java_fields = '\n'.join(java_fields)

    ctr_assign = '\n'.join([
        f'            this.{f.name} = {f.name};' for f in filter(lambda x: not x.lazy, clazz.fields)
    ] + [
        f'            this.{f.name} = {f.default};' for f in filter(lambda x: x.default is not None, clazz.fields)
    ])

    extends = ''
    super_init = ''
    if clazz.extends is not None:
        extends = f'extends {clazz.extends}'
        superf = ', '.join([
            f'{f.name}' for f in clazz.super_fields
        ])
        super_init = f'super({superf});'

    to_string_values = ', " + \n                "'.join([
        generate_toString(f, clazz) for f in all_class_fields
    ])
    to_string_flat_values = ', " + \n                "'.join([
        generate_toFlatString(f, clazz) for f in all_class_fields
    ])

    return f"""
    public static class {class_name} {extends} {cbo}
{java_fields}
        public {class_name}({', '.join(ctr_params)}) {cbo}
            {super_init}
{ctr_assign}
        {cbc}

        public String toString() {cbo}
            return "{class_name}({to_string_values})";
        {cbc}

        public String toFlatString() {cbo}
            return "{class_name}({to_string_flat_values})";
        {cbc}

        public CompiledCode compile(CompileContext context) {cbo}
            return ASTVisitor.instance.compile{class_name}(this, context);
        {cbc}
    {cbc}
    """

def generate_visit(class_name, clazz):
    return f'''
    public CompiledCode compile{class_name}({class_name} stmt, CompileContext context) {cbo} 
        return null;
    {cbc}
    '''

data = {}

ast_header = '''
package io.dallen.ast;

import io.dallen.compiler.visitor.ASTVisitor;
import io.dallen.compiler.CompileContext;
import io.dallen.compiler.CompiledCode;
import io.dallen.tokenizer.Token;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"Duplicates", "WeakerAccess", "Convert2MethodRef", "RedundantStringOperation", "OptionalUsedAsFieldOrParameterType"})
public class AST {
'''

ast_footer = '''
}
'''

with open('grammar.json', 'r') as f:
    data = json.load(f)

with open('ast.java', 'w+') as ast:
    with open('visitor.java', 'w+') as visit:
        classes = {name: comile_class(name, spec) for name, spec in data.items()}
        
        for name, clazz in classes.items():
            clazz.super_fields = get_parent_fields(classes, clazz)

        ast.write(ast_header)        
        for name, clazz in classes.items():
            ast_code = generate_def(name, clazz)
            ast.write(ast_code)
        ast.write(ast_footer)

        for name, clazz in classes.items():
            visitor_code = generate_visit(name, clazz) 
            visit.write(visitor_code)

