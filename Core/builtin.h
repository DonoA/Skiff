#pragma once
#include <vector>
#include <functional>
#include "types.h"

using std::vector;

class navite_builtin
{
public:
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_add();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_sub();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_mul();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_div();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_mod();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_clone();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_to_string();
};

class string_builtin
{
public:
	static object * to_string(object * self, vector<object *> params, scope * env);
	static object * clone(object * self, vector<object *> params, scope * env);
};

class builtin
{

public:
	enum type { Int, Float, Double, Char, Long, String, None };
	static type get_type_for(size_t id);
	static size_t get_id_for(type nt);
	static string get_name_for(type nt);
	static string get_c_type_for(builtin::type nt);
};

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_add()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T * n1 = (T *)self->get_value();
		T * n2 = (T *)params[0]->get_value();
		(*n1) = (*n1) + (*n2);
		return nullptr;
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_sub()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T * n1 = (T *)self->get_value();
		T * n2 = (T *)params[0]->get_value();
		(*n1) = (*n1) - (*n2);
		return nullptr;
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_mul()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T * n1 = (T *)self->get_value();
		T * n2 = (T *)params[0]->get_value();
		(*n1) = (*n1) * (*n2);
		return nullptr;
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_div()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T * n1 = (T *)self->get_value();
		T * n2 = (T *)params[0]->get_value();
		(*n1) = (*n1) / (*n2);
		return nullptr;
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_mod()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T * n1 = (T *)self->get_value();
		T * n2 = (T *)params[0]->get_value();
		(*n1) = (*n1) % (*n2);
		return nullptr;
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_clone()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T v = *(T *)self->get_value();
		return new object(object::allocate(v), self->get_type());
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_to_string()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T v = *(T *)self->get_value();
		string * s = new string(std::to_string(v));
		return new object((void *)s, env->get_type("String"));
	});
}
