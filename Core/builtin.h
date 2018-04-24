#pragma once
#include <vector>
#include <functional>
#include <string>
#include <iostream>
#include <map>
#include "types.h"
#include "utils.h"

namespace skiff
{
	namespace builtin
	{
		enum type { Int, Float, Double, Char, Long, String, None };
		type get_type_for(size_t id);
		size_t get_id_for(type nt);
		std::string get_name_for(type nt);
		std::string get_c_type_for(type nt);

		namespace generator
		{
			template<class T>
			std::function<environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>, 
				environment::scope *)> * create_add();
			template<class T>
			std::function<environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>, 
				environment::scope *)> * create_sub();
			template<class T>
			std::function<environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>, 
				environment::scope *)> * create_mul();
			template<class T>
			std::function<environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>,
				environment::scope *)> * create_div();
			template<class T>
			std::function<environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>, 
				environment::scope *)> * create_mod();
			template<class T>
			std::function<environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>,
				environment::scope *)> * create_clone();
			template<class T>
			std::function<environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>, 
				environment::scope *)> * create_to_string();
			template<class T>
			std::function<environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>,
				environment::scope *)> * create_constructor();

			namespace string_builtins
			{
				environment::skiff_object to_string(environment::skiff_object self, 
					std::vector<environment::skiff_object> params, environment::scope * env);
				environment::skiff_object clone(environment::skiff_object self, 
					std::vector<environment::skiff_object> params, environment::scope * env);
			}
			template<class T>
			inline std::function<environment::skiff_object(environment::skiff_object, 
				std::vector<environment::skiff_object>, environment::scope*)>* create_add()
			{
				return new std::function<
					environment::skiff_object(environment::skiff_object,std::vector<environment::skiff_object>, environment::scope *)>(
						[](environment::skiff_object self, std::vector<environment::skiff_object> params, 
							environment::scope * env)
				{
					T * n1 = (T *)self.get_value();
					T * n2 = (T *)params[0].get_value();
					(*n1) = (*n1) + (*n2);
					return environment::skiff_object();
				});
			}

			template<class T>
			inline std::function<environment::skiff_object(environment::skiff_object,
				std::vector<environment::skiff_object>, environment::scope*)>* create_sub()
			{
				return new std::function<
					environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>, environment::scope *)>(
						[](environment::skiff_object self, std::vector<environment::skiff_object> params,
							environment::scope * env)
				{
					T * n1 = (T *)self.get_value();
					T * n2 = (T *)params[0].get_value();
					(*n1) = (*n1) - (*n2);
					return environment::skiff_object();
				});
			}

			template<class T>
			inline std::function<environment::skiff_object(environment::skiff_object,
				std::vector<environment::skiff_object>, environment::scope*)>* create_mul()
			{
				return new std::function<
					environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>, environment::scope *)>(
						[](environment::skiff_object self, std::vector<environment::skiff_object> params,
							environment::scope * env)
				{
					T * n1 = (T *)self.get_value();
					T * n2 = (T *)params[0].get_value();
					(*n1) = (*n1) * (*n2);
					return environment::skiff_object();
				});
			}

			template<class T>
			inline std::function<environment::skiff_object(environment::skiff_object, 
				std::vector<environment::skiff_object>, environment::scope*)>* create_div()
			{
				return new std::function<
					environment::skiff_object(environment::skiff_object,std::vector<environment::skiff_object>, environment::scope *)>(
						[](environment::skiff_object self, std::vector<environment::skiff_object> params, 
							environment::scope * env)
				{
					T * n1 = (T *)self.get_value();
					T * n2 = (T *)params[0].get_value();
					(*n1) = (*n1) / (*n2);
					return environment::skiff_object();
				});
			}

			template<class T>
			inline std::function<environment::skiff_object(environment::skiff_object,
				std::vector<environment::skiff_object>, environment::scope*)>* create_mod()
			{
				return new std::function<
					environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>, environment::scope *)>(
						[](environment::skiff_object self, std::vector<environment::skiff_object> params,
							environment::scope * env)
				{
					T * n1 = (T *)self.get_value();
					T * n2 = (T *)params[0].get_value();
					(*n1) = (*n1) % (*n2);
					return environment::skiff_object();
				});
			}

			template<class T>
			inline std::function<environment::skiff_object(environment::skiff_object,
				std::vector<environment::skiff_object>, environment::scope*)>* create_clone()
			{
				return new std::function<
					environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>, environment::scope *)>(
						[](environment::skiff_object self, std::vector<environment::skiff_object> params,
							environment::scope * env)
				{
					T v = *(T *)self.get_value();
					return environment::skiff_object(::skiff::utils::allocate(v), self.get_class());
				});
			}

			template<class T>
			inline std::function<environment::skiff_object(environment::skiff_object,
				std::vector<environment::skiff_object>, environment::scope*)>* create_to_string()
			{
				return new std::function<
					environment::skiff_object(environment::skiff_object, std::vector<environment::skiff_object>, environment::scope *)>(
						[](environment::skiff_object self, std::vector<environment::skiff_object> params,
							environment::scope * env)
				{
					T v = *(T *)self.get_value();
					std::string * s = new std::string(std::to_string(v));
					return environment::skiff_object((void *)s, env->get_type("String"));
				});
			}
			template<class T>
			std::function<environment::skiff_object(std::vector<environment::skiff_object>, 
				environment::scope*)>* create_constructor()
			{
				return new std::function<
					environment::skiff_object(std::vector<environment::skiff_object>, 
						environment::scope *)>(
						[](std::vector<environment::skiff_object> params, environment::scope * env)
				{
					T * v = (T *) malloc(sizeof(T));
					*v = *((T *) params[0].get_value());
					return environment::skiff_object((void *)s, env->get_type("String"));
				});
			}
		}

		namespace load
		{
			template<class T>
			environment::skiff_class define_native_builtins(environment::scope * env, builtin::type nt);
			template<class T>
			environment::skiff_class define_native_fixpoint_builtins(environment::scope * env, builtin::type nt);
			environment::skiff_class define_string_builtins(environment::scope * env);
			void load_standards(environment::scope * env);

			template<class T>
			inline environment::skiff_class define_native_builtins(environment::scope * env, builtin::type nt)
			{
				environment::skiff_class t = environment::skiff_class(builtin::get_name_for(nt)/*, builtin::get_id_for(nt)*/);
				(*t.get_operators())[std::string(1, '+')] = environment::skiff_function("add", env, 
					generator::create_add<T>());
				(*t.get_operators())[std::string(1, '-')] = environment::skiff_function("sub", env,
					generator::create_sub<T>());
				(*t.get_operators())[std::string(1, '*')] = environment::skiff_function("mul", env,
					generator::create_mul<T>());
				(*t.get_operators())[std::string(1, '/')] = environment::skiff_function("div", env,
					generator::create_div<T>());
				t.get_scope()->define_function("to_string", environment::skiff_function("to_string", env,
					generator::create_to_string<T>()));
				t.get_scope()->define_function("clone", environment::skiff_function("clone", env,
					generator::create_clone<T>()));
				t.add_constuctor(environment::skiff_function("constructor", env, 
					generator::create_constructor<T>()));
				return t;
			}

			template<class T>
			inline environment::skiff_class define_native_fixpoint_builtins(environment::scope * env,
				builtin::type nt)
			{
				environment::skiff_class t = define_native_builtins<T>(env, nt);
				(*t.get_operators())[std::string(1, '%')] = environment::skiff_function("mod", 
					env, generator::create_mod<T>());
				return t;
			}
		}

		namespace utils
		{
			environment::skiff_object * get_dominant_type(environment::skiff_object * c1, environment::skiff_object * c2);
		}
	}
}








