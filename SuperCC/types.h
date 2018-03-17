#pragma once
#include <string>

using std::string;

class type
{
public:
	type(string val);
	virtual string to_string();
	virtual string parse_string();
private:
	string val;
};

class TypeClass : public type
{
public:
	TypeClass(string name);
	virtual string to_string();
	virtual string parse_string();
private:
	string name;
};

class String : public type
{
public:
	String(string raw);
	string to_string();
	string parse_string();
private:
	string value;
};

class Int : public type
{
public:
	Int(string raw);
	string to_string();
	string parse_string();
private:
	int value;
};

class Double : public type
{
public:
	Double(string raw);
	string to_string();
	string parse_string();
private:
	double value;
};

class Boolean : public type
{
public:
	Boolean(string raw);
	string to_string();
	string parse_string();
private:
	bool value;
};

class None : public type
{
public:
	None();
	string to_string();
	string parse_string();
};