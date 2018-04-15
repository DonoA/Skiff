#include "stdafx.h"
#include "builtin.h"
#include <iostream>

object * string_builtin::to_string(object * self, vector<object*> params, scope * env)
{
	return self;
}

object * string_builtin::clone(object * self, vector<object*> params, scope * env)
{
	return new object((void *) new string(*(string *)self->get_value()), self->get_type());
}

static const builtin::type type_by_id[] = {
	builtin::type::None,
	builtin::type::Char,
	builtin::type::Int,
	builtin::type::Long,
	builtin::type::Float,
	builtin::type::Double,
	builtin::type::String
};

static const map<builtin::type, string> name_by_type = {
	{ builtin::type::Char, "Char" },
	{ builtin::type::Int, "Int" },
	{ builtin::type::Long, "Long" },
	{ builtin::type::Float, "Float" },
	{ builtin::type::Double, "Double" },
	{ builtin::type::String, "String" }
};

static const map<builtin::type, string> cname_by_type = {
	{ builtin::type::Char, "char" },
	{ builtin::type::Int, "int" },
	{ builtin::type::Long, "long" },
	{ builtin::type::Float, "float" },
	{ builtin::type::Double, "double" },
	{ builtin::type::String, "char *" }
};

string builtin::get_c_type_for(builtin::type nt)
{
	if (cname_by_type.count(nt))
	{
		return cname_by_type.at(nt);
	}
	return "None";
}

builtin::type builtin::get_type_for(size_t id)
{
	if (id < sizeof(type_by_id) / sizeof(*type_by_id))
	{
		return type_by_id[id];
	}
	return None;
}

size_t builtin::get_id_for(builtin::type nt)
{
	for (size_t i = 0; i < sizeof(type_by_id) / sizeof(*type_by_id); i++)
	{
		if (type_by_id[i] == nt)
		{
			return i;
		}
	}
	return None;
}

string builtin::get_name_for(builtin::type nt)
{
	if (name_by_type.count(nt))
	{
		return name_by_type.at(nt);
	}
	return "None";
}

type_class builtin_load::define_string_builtins(scope * env)
{
	type_class t = type_class("String", builtin::type::String);
	(*t.get_operators())[string(1, '+')] = function("add", env, navite_builtin::create_add<string>());
	t.get_scope()->define_function("to_string", function("to_string", env, new std::function<object*(object*, vector<object*>, scope*)>(&string_builtin::to_string)));
	t.get_scope()->define_function("clone", function("clone", env, new std::function<object*(object*, vector<object*>, scope*)>(&string_builtin::clone)));
	return t;
}

void builtin_load::load_standards(scope * env)
{
	env->define_type(builtin::get_name_for(builtin::type::Char), define_native_fixpoint_builtins<char>(env, builtin::type::Char));
	env->define_type(builtin::get_name_for(builtin::type::Int), define_native_fixpoint_builtins<int>(env, builtin::type::Int));
	env->define_type(builtin::get_name_for(builtin::type::Long), define_native_fixpoint_builtins<long>(env, builtin::type::Long));
	env->define_type(builtin::get_name_for(builtin::type::Float), define_native_builtins<float>(env, builtin::type::Float));
	env->define_type(builtin::get_name_for(builtin::type::Double), define_native_builtins<double>(env, builtin::type::Double));
	env->define_type(builtin::get_name_for(builtin::type::String), define_string_builtins(env));
}
