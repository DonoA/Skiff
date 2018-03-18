#include "stdafx.h"
#include "statement.h"
#include "utils.h"
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

value::value(object * val)
{
	this->val = val;
	this->typ = val->get_type();
}

object * value::eval(scope * env)
{
	return val;
}

string value::eval_c()
{
	return val->to_string();
}

string value::parse_string()
{
	return "Value(" + typ.parse_string() + "," + val->to_string() + ")";
}

decleration::decleration(string name, type_class type, statement * value)
{
	this->name = name;
	this->val = value;
	this->type = type;
}

object * decleration::eval(scope * env)
{
	env->define_variable(name, val->eval(env));
	return nullptr;
}

string decleration::eval_c()
{
	return "";// type + " " + name + " = " + val->eval_c() + ";";
}

string decleration::parse_string()
{
	return "Decleration(" + name + "," + type.parse_string() + "," + val->parse_string() + ")";
}

function_call::function_call(string name, vector<statement*> params)
{
	this->name = remove_pad(name);
	this->params = params;
}

object * function_call::eval(scope * env)
{
	if (name == "print")
	{
		string tp;
		for (statement * stmt : params)
		{
			object * res = stmt->eval(env);
			res = res->get_type().get_scope()->get_function("to_string").eval(res);
			tp += *((string *) res->get_value());
			tp += " ";
		}
		tp.erase(tp.length() - 1);
		std::cout << tp << std::endl;
	}
	return nullptr;
}

string function_call::eval_c()
{
	string rtn = name + "(";
	for (statement * stmt : params)
	{
		rtn += stmt->eval_c() + ",";
	}
	rtn = rtn.substr(0, rtn.length() - 1);
	rtn += ");";
	return rtn;
}

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

object * variable::eval(scope * env)
{
	return env->get_variable(name);
}

string variable::eval_c()
{
	return name;
}

string variable::parse_string()
{
	return "Variable(" + name + ")";
}

assignment::assignment(string name, statement * value)
{
	this->name = name;
	this->val = value;
}

object * assignment::eval(scope * env)
{
	object * v = val->eval(env);
	env->define_variable(name, v);
	return v;
}

string assignment::eval_c()
{
	return name + "=" + val->eval_c() + ";";
}

string assignment::parse_string()
{
	return "Assignment(" + name + "," + val->parse_string() + ")";
}

math_statement::math_statement(queue<statement*> operands, queue<char> operators)
{
	this->operands = operands;
	this->operators = operators;
}

object * math_statement::eval(scope * env)
{
	queue<char> ops = operators;
	queue<statement *> stmts = operands;
	object * base = stmts.front()->eval(env);
	object * t = base->get_type().get_scope()->get_function("clone").eval(base);
	stmts.pop();
	while (!ops.empty())
	{
		math_statement::eval_single_op(t, ops.front(), stmts.front()->eval(env));
		stmts.pop();
		ops.pop();
	}
	return t;
}

string math_statement::eval_c()
{
	return string();
}

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

object * comparison::eval(scope * env)
{
	return nullptr;
}

string comparison::eval_c()
{
	return string();
}

string comparison::parse_string()
{
	return "Comparison(" + s1->parse_string() + " " + this->comparison_string() + " " + s2->parse_string() + ")";
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

object * invert::eval(scope * env)
{
	return nullptr;
}

string invert::eval_c()
{
	return "!" + val->eval_c();
}

string invert::parse_string()
{
	return "Invert(" + val->parse_string() + ")";
}

bitinvert::bitinvert(statement * value)
{
	this->val = value;
}

object * bitinvert::eval(scope * env)
{
	return nullptr;
}

string bitinvert::eval_c()
{
	return "~" + val->eval_c();
}

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

object * bitwise::eval(scope * env)
{
	return nullptr;
}

string bitwise::eval_c()
{
	return string();
}

string bitwise::parse_string()
{
	return "Bitwise(" + s1->parse_string() + " " + this->operation_string() + " " + s2->parse_string() + ")";
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
	}
	return "";
}

boolean_conjunction::boolean_conjunction(comparison * s1, boolean_conjunction::conjunction_type conj, comparison * s2)
{
	this->s1 = s1;
	this->s2 = s2;
	this->conj = conj;
}

object * boolean_conjunction::eval(scope * env)
{
	return nullptr;
}

string boolean_conjunction::eval_c()
{
	return string();
}

string boolean_conjunction::parse_string()
{
	return "BooleanConjunction(" + s1->parse_string() + " " + this->conj_string() + " " + s2->parse_string() + ")";
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

void block_heading::add_statement(statement * stmt)
{
	statements.push_back(stmt);
}

if_heading::if_heading(statement * condition)
{
	this->condition = condition;
}

object * if_heading::eval(scope * env)
{
	return condition->eval(env);
}

string if_heading::eval_c()
{
	return "if(" + condition->eval_c() + ")";
}

string if_heading::parse_string()
{
	string rtn = "If(" + condition->parse_string() + ")\n{\n";
	for (statement * s : statements)
	{
		rtn += "" + s->parse_string() + "\n";
	}
	rtn += "}";
	return rtn;
}

class_heading::class_heading(string name)
{
	this->name = name;
}

object * class_heading::eval(scope * env)
{
	return nullptr;
}

string class_heading::eval_c()
{
	return string();
}

string class_heading::parse_string()
{
	string rtn = "ClassHeading(" + name + ")\n{\n";
	for (statement * s : statements)
	{
		rtn += "" + s->parse_string() + "\n";
	}
	rtn += "}";
	return rtn;
}


function_heading::function_heading(string name, vector<function::function_parameter> params, type_class returns)
{
	this->name = name;
	this->params = params;
	this->returns = returns;
}

object * function_heading::eval(scope * env)
{
	return nullptr;
}

string function_heading::eval_c()
{
	return string();
}

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
	string rtn = "FunctionHeading(" + name + ", " + params_rtn + ", Returns(" + returns.parse_string() + "))\n{\n";
	for (statement * s : statements)
	{
		rtn += "" + s->parse_string() + "\n";
	}
	rtn += "}";
	return rtn;
}

string function_heading::function_parameter_sig(function::function_parameter p)
{
	return "Param(" + p.name + +"," + p.typ.parse_string() + ")";
}

while_heading::while_heading(statement * condition)
{
	this->condition = condition;
}

object * while_heading::eval(scope * env)
{
	return nullptr;
}

string while_heading::eval_c()
{
	return "while(" + condition->eval_c() + ")";
}

string while_heading::parse_string()
{
	string rtn = "While(" + condition->parse_string() + ")\n{\n";
	for (statement * s : statements)
	{
		rtn += "" + s->parse_string() + "\n";
	}
	rtn += "}";
	return rtn;
}
