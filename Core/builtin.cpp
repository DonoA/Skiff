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
