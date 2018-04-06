#include "stdafx.h"
#include "statement.h"
#include "utils.h"
#include "builtin.h"
#include <iostream>

statement::statement() { }

statement::statement(string raw)
{
	this->raw = raw;
}

string statement::eval_c()
{
	return raw;
}

object * statement::eval(scope * env)
{
	return nullptr;
}

string statement::parse_string()
{
	return "Statement("+raw+")";
}

int statement::indent_mod()
{
	return 0;
}

scope::scope(scope * inherit)
{
	this->inherit = inherit;
}

void scope::define_variable(string name, object * val)
{
	env[name] = val;
}

object * scope::get_variable(string name)
{
	if (env.count(name))
	{
		return env[name];
	}
	if (inherit == nullptr)
	{
		return nullptr;
	}
	return inherit->get_variable(name);
}

void scope::define_type(string name, type_class cls)
{
	known_types[name] = cls;
}

type_class scope::get_type(string name)
{
	if (known_types.count(name))
	{
		return known_types[name];
	}
	if (inherit == nullptr)
	{
		return type_class();
	}
	return inherit->get_type(name);
}

void scope::define_function(string name, function cls)
{
	known_functions[name] = cls;
}

function scope::get_function(string name)
{
	if (known_functions.count(name))
	{
		return known_functions[name];
	}
	if (inherit == nullptr)
	{
		return function();
	}
	return inherit->get_function(name);
}

value::value(string val)
{
	this->val = val;
}

//object * value::eval(scope * env)
//{
//	return val;
//}
//
//string value::eval_c()
//{
//	return *(string *) typ.get_scope()->get_function("to_string").eval(val)->get_value();
//}

string value::parse_string()
{
	return "Value(" + val + ")";
}

decleration::decleration(string name, type_class type)
{
	this->name = name;
	this->type = type;
}

//object * decleration::eval(scope * env)
//{
//	env->define_variable(name, val->eval(env));
//	return nullptr;
//}
//
//string decleration::eval_c()
//{
//	return type.get_name() + " " + name + " = " + val->eval_c();
//}

string decleration::parse_string()
{
	return "Decleration(" + name + "," + type.parse_string() + ")";
}

function_call::function_call(string name, vector<statement*> params)
{
	this->name = remove_pad(name);
	this->params = params;
}

//object * function_call::eval(scope * env)
//{
//	if (name == "print")
//	{
//		string tp;
//		for (statement * stmt : params)
//		{
//			object * res = stmt->eval(env);
//			res = res->get_type().get_scope()->get_function("to_string").eval(res);
//			tp += *((string *) res->get_value());
//			tp += " ";
//		}
//		tp.erase(tp.length() - 1);
//		std::cout << tp << std::endl;
//	}
//	return nullptr;
//}
//
//string function_call::eval_c()
//{
//	string rtn = name + "(";
//	for (statement * stmt : params)
//	{
//		rtn += stmt->eval_c() + ",";
//	}
//	rtn = rtn.substr(0, rtn.length() - 1);
//	rtn += ");";
//	return rtn;
//}

string function_call::parse_string()
{
	string rtn = "FunctionCall(" + name + ", Params(";
	bool any = false;
	for (statement * stmt : params)
	{
		rtn += stmt->parse_string() + ",";
		any = true;
	}
	if (any)
	{
		rtn = rtn.substr(0, rtn.length() - 1);
	}
	rtn += "))";
	return rtn;
}

variable::variable(string name) : statement(name)
{
	this->name = name;
}

//object * variable::eval(scope * env)
//{
//	return env->get_variable(name);
//}
//
//string variable::eval_c()
//{
//	return name;
//}

string variable::parse_string()
{
	return "Variable(" + name + ")";
}

assignment::assignment(string name, statement * value)
{
	this->name = name;
	this->val = value;
}

//object * assignment::eval(scope * env)
//{
//	object * v = val->eval(env);
//	env->define_variable(name, v);
//	return v;
//}
//
//string assignment::eval_c()
//{
//	return name + "=" + val->eval_c();
//}

string assignment::parse_string()
{
	return "Assignment(" + name + "," + val->parse_string() + ")";
}

math_statement::math_statement(queue<statement*> operands, queue<char> operators)
{
	this->operands = operands;
	this->operators = operators;
}

//object * math_statement::eval(scope * env)
//{
//	queue<char> ops = operators;
//	queue<statement *> stmts = operands;
//	object * base = stmts.front()->eval(env);
//	object * t = base->get_type().get_scope()->get_function("clone").eval(base);
//	stmts.pop();
//	while (!ops.empty())
//	{
//		math_statement::eval_single_op(t, ops.front(), stmts.front()->eval(env));
//		stmts.pop();
//		ops.pop();
//	}
//	return t;
//}
//
//string math_statement::eval_c()
//{
//	string rtn = string();
//	queue<char> ops = operators;
//	queue<statement *> stmts = operands;
//	while (!ops.empty())
//	{
//		rtn += stmts.front()->eval_c() + " " + ops.front() + " ";
//		stmts.pop();
//		ops.pop();
//	}
//	rtn += stmts.front()->eval_c();
//	return rtn;
//}

string math_statement::parse_string()
{
	string rtn = "MathStatement(";
	queue<char> ops = operators;
	queue<statement *> stmts = operands;
	while (!ops.empty())
	{
		rtn += stmts.front()->parse_string() + " " + ops.front() + " ";
		stmts.pop();
		ops.pop();
	}
	rtn += stmts.front()->parse_string() + ")";
	return rtn;
}

void math_statement::eval_single_op(object * s1, char op, object * s2)
{
	object * o = get_dominant_type(s1, s2);
	if (o == nullptr)
	{
		o = s1;
	}
	vector<object *> p;
	p.push_back(s2);
	(*o->get_type().get_operators())[string(1, op)].eval(o, p);
}

comparison::comparison(statement * s1, comparison::comparison_type typ, statement * s2)
{
	this->s1 = s1;
	this->s2 = s2;
	this->typ = typ;
}

//object * comparison::eval(scope * env)
//{
//	return nullptr;
//}
//
//string comparison::eval_c()
//{
//	return s1->eval_c() + " " + this->comparison_string() + " " + s2->eval_c();
//}

string comparison::parse_string()
{
	return "Comparison(" + s1->parse_string() + " " + this->comparison_string() + " " + 
		s2->parse_string() + ")";
}

string comparison::comparison_string()
{
	switch (typ)
	{
	case Equal:
		return "==";
	case NotEqual:
		return "!=";
	case LessThan:
		return "<";
	case LessThanEqualTo:
		return "<=";
	case GreaterThan:
		return ">";
	case GreaterThanEqualTo:
		return ">=";
	}
	return "";
}

invert::invert(statement * value)
{
	this->val = value;
}

//object * invert::eval(scope * env)
//{
//	return nullptr;
//}
//
//string invert::eval_c()
//{
//	return "!" + val->eval_c();
//}

string invert::parse_string()
{
	return "Invert(" + val->parse_string() + ")";
}

bitinvert::bitinvert(statement * value)
{
	this->val = value;
}

//object * bitinvert::eval(scope * env)
//{
//	return nullptr;
//}
//
//string bitinvert::eval_c()
//{
//	return "~" + val->eval_c();
//}

string bitinvert::parse_string()
{
	return "BitInvert(" + val->parse_string() + ")";
}

bitwise::bitwise(statement * s1, bitwise::operation op, statement * s2)
{
	this->s1 = s1;
	this->op = op;
	this->s2 = s2;
}

//object * bitwise::eval(scope * env)
//{
//	return nullptr;
//}
//
//string bitwise::eval_c()
//{
//	return string();
//}

string bitwise::parse_string()
{
	return "Bitwise(" + s1->parse_string() + " " + this->operation_string() + " " + 
		s2->parse_string() + ")";
}

string bitwise::operation_string()
{
	switch (op)
	{
	case And:
		return "&";
	case Or:
		return "|";
	case Xor:
		return "^";
	case ShiftLeft:
		return "<<";
	case ShiftRight:
		return ">>";
	}
	return "";
}

boolean_conjunction::boolean_conjunction(comparison * s1, 
	boolean_conjunction::conjunction_type conj, comparison * s2)
{
	this->s1 = s1;
	this->s2 = s2;
	this->conj = conj;
}

//object * boolean_conjunction::eval(scope * env)
//{
//	return nullptr;
//}
//
//string boolean_conjunction::eval_c()
//{
//	return s1->eval_c() + " " + this->conj_string() + " " + s2->eval_c();
//}

string boolean_conjunction::parse_string()
{
	return "BooleanConjunction(" + s1->parse_string() + " " + this->conj_string() + " " + 
		s2->parse_string() + ")";
}

string boolean_conjunction::conj_string()
{
	switch (conj)
	{
	case And:
		return "&&";
	case Or:
		return "||";
	}
	return "";
}

block_heading::block_heading(string raw) : statement(raw)
{
	this->raw = raw;
}

string block_heading::eval_c()
{
	return string();
}

object * block_heading::eval(scope * env)
{
	return nullptr;
}

int block_heading::indent_mod()
{
	return 1;
}

if_heading::if_heading(statement * condition)
{
	this->condition = condition;
}

//object * if_heading::eval(scope * env)
//{
//	return condition->eval(env);
//}
//
//string if_heading::eval_c()
//{
//	return "if(" + condition->eval_c() + ")";
//}

string if_heading::parse_string()
{
	return "If(" + condition->parse_string() + ")";
}

//object * class_heading::eval(scope * env)
//{
//	return nullptr;
//}
//
//string class_heading::eval_c()
//{
//	return "NOT CONVERTABLE";
//}

class_heading::class_heading(class_heading::class_type type, string name)
{
	this->type = type;
	this->name = name;
}

string class_heading::parse_string()
{
	switch (type)
	{
	case CLASS:
		return "ClassHeading(" + name + ")";
	case STRUCT:
		return "StructHeading(" + name + ")";
	}
	return string();
}

string class_heading::get_name()
{
	return name;
}


function_heading::function_heading(string name, vector<function::function_parameter> params, 
	type_class returns)
{
	this->name = name;
	this->params = params;
	this->returns = returns;
}

//object * function_heading::eval(scope * env)
//{
//	return nullptr;
//}
//
//string function_heading::eval_c()
//{
//	string params_rtn = string();
//	bool any = false;
//	for (function::function_parameter p : params)
//	{
//		params_rtn += function_parameter_c_sig(p) + ",";
//		any = true;
//	}
//	if (any)
//	{
//		params_rtn = params_rtn.substr(0, params_rtn.length() - 1);
//	}
//	return returns.get_name() + " " + name + "(" + params_rtn + ")";
//}

string function_heading::parse_string()
{
	string params_rtn = "Params(";
	bool any = false;
	for (function::function_parameter p : params)
	{
		params_rtn += function_parameter_sig(p) + ",";
		any = true;
	}
	if (any)
	{
		params_rtn = params_rtn.substr(0, params_rtn.length() - 1);
	}
	params_rtn += ")";
	return "FunctionHeading(" + name + ", " + params_rtn + 
		", Returns(" + returns.parse_string() + "))";
}

string function_heading::function_parameter_sig(function::function_parameter p)
{
	return "Param(" + p.name + +"," + p.typ.parse_string() + ")";
}

string function_heading::function_parameter_c_sig(function::function_parameter p)
{
	builtin::type t = builtin::get_type_for(p.typ.get_class_id());
	return builtin::get_c_type_for(t) + " " + p.name;
}

while_heading::while_heading(statement * condition)
{
	this->condition = condition;
}

//object * while_heading::eval(scope * env)
//{
//	return nullptr;
//}
//
//string while_heading::eval_c()
//{
//	return "while(" + condition->eval_c() + ")";
//}

string while_heading::parse_string()
{
	return "While(" + condition->parse_string() + ")";
}

string end_block_statement::parse_string()
{
	return "EndBlock()";
}

int end_block_statement::indent_mod()
{
	return -1;
}

return_statement::return_statement(statement * returns)
{
	this->returns = returns;
}

//object * return_statement::eval(scope * env)
//{
//	return nullptr;
//}
//
//string return_statement::eval_c()
//{
//	return "return " + returns->eval_c();
//}

string return_statement::parse_string()
{
	return "Returns(" + returns->parse_string() + ")";
}

new_object_statement::new_object_statement(type_class type, vector<statement*> params)
{
	this->type = type;
	this->params = params;
}

//object * new_object_statement::eval(scope * env)
//{
//	return nullptr;
//}
//
//string new_object_statement::eval_c()
//{
//	return "NOT CONVERTABLE";
//}

string new_object_statement::parse_string()
{
	string paramz;
	bool any = false;
	for (statement * p : params)
	{
		paramz +=  p->parse_string() + ",";
		any = true;
	}
	if (any)
	{
		paramz = paramz.substr(0, paramz.length() - 1);
	}
	return "New(" + type.parse_string() + ", Params(" + paramz + ")";
}

annotation_tag::annotation_tag(string tag_name, vector<statement *> params, 
	statement * on) : modifier_base(on)
{
	name = tag_name;
	this->params = params;
}

string annotation_tag::parse_string()
{
	string parms;
	bool any = false;
	for (statement * stmt : params)
	{
		parms += stmt->parse_string() + ",";
		any = true;
	}
	if (any)
	{
		parms = parms.substr(0, parms.length() - 1);
	}
	return "Annotation(" + name + ", Params(" + parms + "), On(" + on->parse_string() + "))";
}

enum_heading::enum_heading(string name)
{
	this->basetype = nullptr;
	this->name = name;
}

enum_heading::enum_heading(string name, class_heading * basetype) : enum_heading(name)
{
	this->basetype = basetype;
}

string enum_heading::parse_string()
{
	string rtn = "Enum(" + name;
	if (basetype != nullptr)
	{
		rtn += ", " + basetype->parse_string() + ")";
	}
	rtn += ")";
	return rtn;
}

modifier::modifier(modifier::modifier_type type, statement * modof) : modifier_base(modof)
{
	this->type = type;
}

string modifier::parse_string()
{
	switch (type)
	{
	case STATIC:
		return "StaticMod(" + on->parse_string() + ")";
	case PRIVATE:
		return "PrivateMod(" + on->parse_string() + ")";
	}
	return string();
}

throw_statement::throw_statement(statement * throws)
{
	this->throws = throws;
}

string throw_statement::parse_string()
{
	return "Throw(" + throws->parse_string() + ")";
}

modifier_base::modifier_base(statement * on)
{
	this->on = on;
}

int modifier_base::indent_mod()
{
	return on->indent_mod();
}

self_modifier::self_modifier(self_modifier::modifier_type type, 
	self_modifier::modifier_time time, statement * on) : modifier_base(on)
{
	this->type = type;
	this->time = time;
}

string self_modifier::parse_string()
{
	string name;
	switch (time)
	{
	case PRE:
		name = "Pre";
		break;
	case POST:
		name = "Post";
		break;
	}
	switch (type)
	{
	case PLUS:
		name += "Increment";
		break;
	case MINUS:
		name += "Decriment";
		break;
	}
	return name + "(" + on->parse_string() + ")";
}

decleration_with_assignment::decleration_with_assignment(string name, 
	type_class type, statement * val)
{
	this->name = name;
	this->type = type;
	this->value = val;
}

string decleration_with_assignment::parse_string()
{
	return "DeclareAndAssign(" + name + ", " + type.parse_string() + ", " + 
		value->parse_string() + ")";
}
