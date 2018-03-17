#include "stdafx.h"
#include "types.h"
#include "utils.h"

type::type(string val)
{
	this->val = val;
}

string type::to_string()
{
	return val;
}

string type::parse_string()
{
	return "Type(" + val + ")";
}

Int::Int(string raw) : type(raw)
{
	value = atoi(raw.c_str());
}

string Int::to_string()
{
	return std::to_string(value);
}

string Int::parse_string()
{
	return "Int(" + std::to_string(value) + ")";
}

String::String(string raw) : type(raw) 
{
	value = raw.substr(1, raw.length() - 2);
}

string String::to_string()
{
	return value;
}

string String::parse_string()
{
	return "String(\"" + value + "\")";
}

Double::Double(string raw) : type(raw)
{
	value = atof(raw.c_str());
}

string Double::to_string()
{
	return std::to_string(value);
}

string Double::parse_string()
{
	return "Double(" + std::to_string(value) + ")";
}

None::None() : type("None")
{
}

string None::to_string()
{
	return "None";
}

string None::parse_string()
{
	return "None()";
}

Boolean::Boolean(string raw) : type(raw)
{
	if (remove_pad(raw) == "true")
	{
		value = true;
	}
	else if(remove_pad(raw) == "false")
	{
		value = false;
	}
}

string Boolean::to_string()
{
	if (value)
	{
		return "true";
	}
	else
	{
		return "false";
	}
}

string Boolean::parse_string()
{
	if (value)
	{
		return "Boolean(true)";
	}
	else
	{
		return "Boolean(false)";
	}
}

TypeClass::TypeClass(string name) : type(name)
{
	this->name = name;
}

string TypeClass::to_string()
{
	return name;
}

string TypeClass::parse_string()
{
	return "TypeClass(" + name + ")";
}