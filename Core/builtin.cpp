#include "stdafx.h"
#include "builtin.h"

static const skiff::builtin::type type_by_id[] = {
	skiff::builtin::type::None,
	skiff::builtin::type::Char,
	skiff::builtin::type::Int,
	skiff::builtin::type::Long,
	skiff::builtin::type::Float,
	skiff::builtin::type::Double,
	skiff::builtin::type::String
};

static const std::map<skiff::builtin::type, std::string> name_by_type = {
	{ skiff::builtin::type::Char, "Char" },
	{ skiff::builtin::type::Int, "Int" },
	{ skiff::builtin::type::Long, "Long" },
	{ skiff::builtin::type::Float, "Float" },
	{ skiff::builtin::type::Double, "Double" },
	{ skiff::builtin::type::String, "String" }
};

static const std::map<skiff::builtin::type, std::string> cname_by_type = {
	{ skiff::builtin::type::Char, "char" },
	{ skiff::builtin::type::Int, "int" },
	{ skiff::builtin::type::Long, "long" },
	{ skiff::builtin::type::Float, "float" },
	{ skiff::builtin::type::Double, "double" },
	{ skiff::builtin::type::String, "char *" }
};

namespace skiff
{
	namespace builtin
	{
		using std::string;
		using std::vector;
		using types::object;
		using types::scope;
		using types::type_class;
		using types::function;

		string get_c_type_for(builtin::type nt)
		{
			if (cname_by_type.count(nt))
			{
				return cname_by_type.at(nt);
			}
			return "None";
		}

		type get_type_for(size_t id)
		{
			if (id < sizeof(type_by_id) / sizeof(*type_by_id))
			{
				return type_by_id[id];
			}
			return None;
		}

		size_t get_id_for(builtin::type nt)
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

		string get_name_for(builtin::type nt)
		{
			if (name_by_type.count(nt))
			{
				return name_by_type.at(nt);
			}
			return "None";
		}

		namespace generator
		{
			namespace string
			{
				object * to_string(object * self, vector<object*> params, scope * env)
				{
					return self;
				}

				object * clone(object * self, vector<object*> params, scope * env)
				{
					return new object((void *) new std::string(*(std::string *)self->get_value()), 
						self->get_type());
				}
			}
		}

		namespace load
		{
			type_class define_string_builtins(scope * env)
			{
				type_class t = type_class("String", builtin::type::String);
				(*t.get_operators())[string(1, '+')] = function("add", env, 
					skiff::builtin::generator::create_add<string>());
				t.get_scope()->define_function("to_string", function("to_string", env, 
					new std::function<object*(object*, vector<object*>, scope*)>(
						&skiff::builtin::generator::string::to_string)));
				t.get_scope()->define_function("clone", function("clone", env, 
					new std::function<object*(object*, vector<object*>, scope*)>(
						&skiff::builtin::generator::string::clone)));
				return t;
			}

			void load_standards(scope * env)
			{
				env->define_type(builtin::get_name_for(builtin::type::Char), 
					define_native_fixpoint_builtins<char>(env, builtin::type::Char));
				env->define_type(builtin::get_name_for(builtin::type::Int), 
					define_native_fixpoint_builtins<int>(env, builtin::type::Int));
				env->define_type(builtin::get_name_for(builtin::type::Long), 
					define_native_fixpoint_builtins<long>(env, builtin::type::Long));
				env->define_type(builtin::get_name_for(builtin::type::Float), 
					define_native_builtins<float>(env, builtin::type::Float));
				env->define_type(builtin::get_name_for(builtin::type::Double), 
					define_native_builtins<double>(env, builtin::type::Double));
				env->define_type(builtin::get_name_for(builtin::type::String), 
					define_string_builtins(env));
			}
		}

		namespace utils
		{
			object * get_dominant_type(object * c1, object * c2)
			{
				builtin::type type_order[] = {
					builtin::type::Double,
					builtin::type::Float,
					builtin::type::Long,
					builtin::type::Int,
					builtin::type::Char
				};
				for (builtin::type s : type_order)
				{
					if (c1->get_type().get_class_id() == builtin::get_id_for(s))
					{
						return c1;
					}
					if (c2->get_type().get_class_id() == builtin::get_id_for(s))
					{
						return c2;
					}
				}
				return nullptr;
			}
		}
	}
}

