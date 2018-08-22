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
        {skiff::builtin::type::Class,    "skiff.lang.Class"},
        {skiff::builtin::type::Struct,   "skiff.lang.Struct"},
        {skiff::builtin::type::Function, "skiff.lang.Function"},

        {skiff::builtin::type::None,     "skiff.lang.None"},
        {skiff::builtin::type::Char,     "skiff.lang.Char"},
        {skiff::builtin::type::Int,      "skiff.lang.Int"},
        {skiff::builtin::type::Long,     "skiff.lang.Long"},
        {skiff::builtin::type::Float,    "skiff.lang.Float"},
        {skiff::builtin::type::Double,   "skiff.lang.Double"},
        {skiff::builtin::type::String,   "skiff.lang.String"},
        {skiff::builtin::type::Boolean,  "skiff.lang.Boolean"}
};

static const std::map<skiff::builtin::type, std::string> cname_by_type = {
        {skiff::builtin::type::Char,   "char"},
        {skiff::builtin::type::Int,    "int"},
        {skiff::builtin::type::Long,   "long"},
        {skiff::builtin::type::Float,  "float"},
        {skiff::builtin::type::Double, "double"},
        {skiff::builtin::type::String, "char *"}
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
        using environment::skiff_value;

        string get_c_type_for(builtin::type nt)
        {
            if (cname_by_type.count(nt))
            {
                return cname_by_type.at(nt);
            }
            return builtin::get_name_for(builtin::type::None);
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
            return builtin::get_name_for(builtin::type::None);
        }

        namespace generator
        {
            namespace strings
            {
                skiff_object to_string(vector<skiff_object> params, scope *env)
                {
                    return params[0];
                }

                skiff_object clone(vector<skiff_object> params, scope *env)
                {
                    return skiff_object(string(*(std::string *) params[0].get_value()->get_value()),
                                        params[0].get_class());
                }
            }

            environment::skiff_object
            function_builtins::print(std::vector<environment::skiff_object> params, environment::scope *env)
            {
                string tp;
                for (skiff_object p : params)
                {
                    tp += p.get_class()->invoke_function("to_string", p).get_value_as<string>();
                    tp += " ";
                }
                tp.erase(tp.length() - 1);
                std::cout << tp << std::endl;

                return env->get_none_object();
            }
        }

        namespace load
        {
            skiff_class *define_string_builtins(scope *env, skiff_class *extnds)
            {
                skiff_class *t = new skiff_class(builtin::get_name_for(builtin::type::String), extnds);
                t->add_operator(environment::builtin_operation::ADD, skiff_function("add", env,
                                                                                    skiff::builtin::generator::create_add<string>()));

                t->add_operator(environment::builtin_operation::EQUAL, skiff_function("equals", env,
                                                                                    skiff::builtin::generator::create_equals<string>()));

                t->get_scope()->define_function("to_string", new skiff_function("to_string", env,
                                                                                new environment::skiff_func_sig(
                                                                                        &skiff::builtin::generator::strings::to_string)));
                t->get_scope()->define_function("clone", new skiff_function("clone", env,
                                                                            new environment::skiff_func_sig(
                                                                                    &skiff::builtin::generator::strings::clone)));
                return t;
            }

            void load_standards(scope *env)
            {
                skiff_class *anyClass = new skiff_class(builtin::get_name_for(builtin::type::Any));

                skiff_class *classClass = new skiff_class(builtin::get_name_for(builtin::type::Class), anyClass);

                env->define_type(anyClass, classClass);
                env->define_type(classClass, classClass);

                env->define_type(define_function_builtins(env, classClass), classClass);

                skiff_class *structClass = new skiff_class(builtin::get_name_for(builtin::type::Struct), true,
                                                           anyClass);

                env->define_type(structClass, classClass);


                env->define_type(define_native_fixpoint_builtins<char>(env, builtin::type::Char, structClass),
                                 classClass);
                env->define_type(define_native_fixpoint_builtins<int>(env, builtin::type::Int, structClass),
                                 classClass);
                env->define_type(define_native_fixpoint_builtins<long>(env, builtin::type::Long, structClass),
                                 classClass);
                env->define_type(define_native_number_builtins<float>(env, builtin::type::Float, structClass),
                                 classClass);
                env->define_type(define_native_number_builtins<double>(env, builtin::type::Double, structClass),
                                 classClass);

                env->define_type(define_string_builtins(env, structClass), classClass);

                env->define_type(define_native_builtins<bool>(env, builtin::type::Boolean, structClass), classClass);

                load_builtin_functions(env);
            }

            environment::skiff_class *define_function_builtins(environment::scope *env, environment::skiff_class *exnds)
            {
                skiff_class * func_class = new skiff_class(builtin::get_name_for(builtin::type::Function), exnds);

//                func_class->get_scope()->define_function("invoke", new skiff_function("invoke", env,
//                                                                                      new environment::skiff_func_sig(
//                                                                                              &skiff::builtin::generator::function_builtins::invoke)));

                return func_class;
            }

            void load_builtin_functions(environment::scope *env)
            {
                env->define_function("print", new skiff_function("print", env,
                                                                 new environment::skiff_func_sig(
                                                                         &skiff::builtin::generator::function_builtins::print)));
            }
        }

        namespace utils
        {
            skiff_object *get_dominant_type(skiff_object *c1, skiff_object *c2)
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

