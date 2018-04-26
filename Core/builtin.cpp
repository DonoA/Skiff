#if (defined (_WIN32) || defined (_WIN64))
	#include "stdafx.h"
#endif
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

		using environment::scope;
		using environment::skiff_object;
		using environment::skiff_class;
		using environment::skiff_function;

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
				skiff_object to_string(vector<skiff_object> params, scope * env)
				{
					return params[0];
				}

				skiff_object clone(vector<skiff_object> params, scope * env)
				{
					return skiff_object((void *) new std::string(*(std::string *)params[0].get_value()),
						params[0].get_class());
				}
			}
		}

		namespace load
		{
			skiff_class define_string_builtins(scope * env)
			{
				skiff_class t = skiff_class("String");
				(*t.get_operators())[string(1, '+')] = skiff_function("add", env, 
					skiff::builtin::generator::create_add<string>());
				t.get_scope()->define_function("to_string", skiff_function("to_string", env,
					new environment::skiff_func_sig(&skiff::builtin::generator::string::to_string)));
				t.get_scope()->define_function("clone", skiff_function("clone", env,
					new environment::skiff_func_sig(&skiff::builtin::generator::string::clone)));
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
			skiff_object * get_dominant_type(skiff_object * c1, skiff_object * c2)
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
					if (c1->get_class()->get_name() == name_by_type.at(s))
					{
						return c1;
					}
					if (c1->get_class()->get_name() == name_by_type.at(s))
					{
						return c2;
					}
				}
				return nullptr;
			}
		}
	}
}

