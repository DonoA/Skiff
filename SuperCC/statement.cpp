#include "stdafx.h"
#include "statement.h"
#include "utils.h"
#include <iostream>

statement::statement(string raw)
{
	this->raw = raw;
}

string statement::eval_c()
{
	return raw;
}

type * statement::eval(scope * env)
{
	return new None();
}

string statement::parse_string()
{
	return "Statement("+raw+")";
}

bool statement::is_block_heading()
{
	return false;
}

value::value(type * typ, string val) : statement(val)
{
	this->typ = typ;
}

type * value::eval(scope * env)
{
	return typ;
}

string value::eval_c()
{
	return typ->to_string();
}

string value::parse_string()
{
	return "Value(" + typ->parse_string() + ")";
}

decleration::decleration(string name, statement * val, string raw) : statement(raw)
{
	this->name = name;
	this->val = val;
}

type * decleration::eval(scope * env)
{
	env->define(name, val->eval(env));
	return val->eval(env);
}

string decleration::eval_c()
{
	return type + " " + name + " = " + val->eval_c() + ";";
}

string decleration::parse_string()
{
	return "Decleration(" + name + "," + val->parse_string() + ")";
}

function_call::function_call(string name, vector<statement*> params, string raw) : statement(raw)
{
	this->name = remove_pad(name);
	this->params = params;
}

type * function_call::eval(scope * env)
{
	/*if (name == "print")
	{
		string tp;
		for (statement * stmt : params)
		{
			tp += stmt->eval(env)->to_string();
			tp += " ";
		}
		tp.erase(tp.length() - 1);
		std::cout << tp << std::endl;
	}*/
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

scope::scope(scope * inherit)
{
	this->inherit = inherit;
}

void scope::define(string name, type * typ)
{
	env[name] = typ;
}

type * scope::get(string name)
{
	if (env.count(name))
	{
		return env[name];
	}
	return nullptr;
}

variable::variable(string name) : statement(name)
{
	this->name = name;
}

type * variable::eval(scope * env)
{
	return env->get(name);
}

string variable::eval_c()
{
	return name;
}

string variable::parse_string()
{
	return "Variable(" + name + ")";
}

assignment::assignment(string name, statement * value, string raw) : statement(raw)
{
	this->name = name;
	this->val = value;
}

type * assignment::eval(scope * env)
{
	type * typ = val->eval(env);
	env->define(name, typ);
	return typ;
}

string assignment::eval_c()
{
	return name + "=" + val->eval_c() + ";";
}

string assignment::parse_string()
{
	return "Assignment(" + name + "," + val->parse_string() + ")";
}

math_statement::math_statement(queue<statement*> operands, queue<char> operators, string raw) : statement(raw)
{
	this->operands = operands;
	this->operators = operators;
}

type * math_statement::eval(scope * env)
{
	return nullptr;
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

comparison::comparison(statement * s1, comparison::comparison_type typ, statement * s2, string raw) : statement(raw)
{
	this->s1 = s1;
	this->s2 = s2;
	this->typ = typ;
}

type * comparison::eval(scope * env)
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

invert::invert(statement * value, string raw) : statement(raw)
{
	this->val = value;
}

type * invert::eval(scope * env)
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

bitinvert::bitinvert(statement * value, string raw) : statement(raw)
{
	this->val = value;
}

type * bitinvert::eval(scope * env)
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

bitwise::bitwise(statement * s1, bitwise::operation op, statement * s2, string raw) : statement(raw)
{
	this->s1 = s1;
	this->op = op;
	this->s2 = s2;
}

type * bitwise::eval(scope * env)
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

boolean_conjunction::boolean_conjunction(comparison * s1, boolean_conjunction::conjunction_type conj, comparison * s2, string raw) : statement(raw)
{
	this->s1 = s1;
	this->s2 = s2;
	this->conj = conj;
}

type * boolean_conjunction::eval(scope * env)
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

type * block_heading::eval(scope * env)
{
	return nullptr;
}

bool block_heading::is_block_heading()
{
	return true;
}

void block_heading::add_statement(statement * stmt)
{
	statements.push_back(stmt);
}

if_heading::if_heading(statement * condition, string raw) : block_heading(raw)
{
	this->condition = condition;
}

type * if_heading::eval(scope * env)
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


class_heading::class_heading(string name, string raw) : block_heading(raw)
{
	this->name = name;
}

type * class_heading::eval(scope * env)
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

function_heading::function_parameter function_heading::create_function_parameter(string name, TypeClass * typ)
{
	struct function_parameter p;
	p.typ = typ;
	p.name = name;
	return p;
}

function_heading::function_heading(string name, vector<function_parameter> params, TypeClass * returns, string raw) : block_heading(raw)
{
	this->name = name;
	this->params = params;
	this->returns = returns;
}

type * function_heading::eval(scope * env)
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
	for (function_parameter p : params)
	{
		params_rtn += function_parameter_sig(p) + ",";
		any = true;
	}
	if (any)
	{
		params_rtn = params_rtn.substr(0, params_rtn.length() - 1);
	}
	string rtn = "FunctionHeading(" + name + ", " + params_rtn + ", Returns(" + returns->parse_string() + "))\n{\n";
	for (statement * s : statements)
	{
		rtn += "" + s->parse_string() + "\n";
	}
	rtn += "}";
	return rtn;
}

string function_heading::function_parameter_sig(function_parameter p)
{
	return "Param(" + p.name + +"," + p.typ->parse_string() + ")";
}

while_heading::while_heading(statement * condition, string raw) : block_heading(raw)
{
	this->condition = condition;
}

type * while_heading::eval(scope * env)
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
