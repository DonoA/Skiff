#include "stdafx.h"
#include "types.h"
#include "utils.h"
#include <iostream>

type_class::type_class()
{
	this->name = "None";
}

type_class::type_class(string name)
{
	this->name = name;
}

string type_class::get_name()
{
	return name;
}

string type_class::parse_string()
{
	return "TypeClass(" + name + ")";
}

scope * type_class::get_scope()
{
	return &class_env;
}

map<string, function>* type_class::get_operators()
{
	return &operators;
}

object::object(void * val, type_class type)
{
	this->type = type;
	this->value = val;
}

type_class object::get_type()
{
	return type;
}

string object::to_string()
{
	return type.get_name() + "@" + std::to_string((int) this);
}

void * object::get_value()
{
	return value;
}

void object::set_value(void * v)
{
	this->value = v;
}

function::function_parameter function::create_function_parameter(string name, type_class typ)
{
	function::function_parameter p;
	p.typ = typ;
	p.name = name;
	return p;
}

function::function(string name, vector<function_parameter> params, type_class returns, scope * env, std::function<object *(object *, vector<object *>, scope *)> * builtin)
{
	this->function_env = scope(env);
	this->name = name;
	this->params = params;
	this->returns = returns;
	this->builtin = builtin;
	
}

function::function(string name, vector<function_parameter> params, type_class returns, scope * env) : function(name, params, returns, env, NULL)
{ }

function::function()
{
	this->builtin = NULL;
}

object * function::eval(object * self)
{
	return eval(self, vector<object*>());
}

object * function::eval(object * self, vector<object*> params)
{
	if (builtin == NULL)
	{
		//for (statement * stmt : statements)
		//{
		//	stmt->eval(&function_env);
		//}
		return nullptr;
	}
	else
	{
		return (*builtin)(self, params, &function_env);
	}
}
