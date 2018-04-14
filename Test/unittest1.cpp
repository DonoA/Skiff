#include "stdafx.h"
#include "CppUnitTest.h"
#include "../Core/statement.h"
#include "../Core/parsers.h"
#include <iostream>

using namespace Microsoft::VisualStudio::CppUnitTestFramework;

namespace Test
{		
	TEST_CLASS(Parsing)
	{
	public:

		TEST_METHOD(Declaration)
		{
			// Decleration(x,TypeClass(Int))
			statement * s, *p;
			s = parse_statement("x: Int");
			p = new decleration("x", type_class("Int"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x: List<Type>");
			vector<type_class> et;
			et.push_back(type_class("Type"));
			p = new decleration("x", type_class("List", et));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(Assignment)
		{
			// Assignment(Statement(x),Value(5))
			statement * s = parse_statement("x = 5");
			statement * p = new assignment(new statement("x"), new value("5"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(AssignmentAndDeclaration)
		{
			// DeclareAndAssign(Statement(x), TypeClass(Int), Value(5))
			statement * s, *p;
			s = parse_statement("x: Int = 5");
			p = new decleration_with_assignment(new statement("x"), type_class("Int"), 
				new value("5"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x: List<Type> = new List<Type>()");
			vector<type_class> et;
			et.push_back(type_class("Type"));
			p = new decleration_with_assignment(new statement("x"), type_class("List", et), 
				new new_object_statement(type_class("List", et), vector<statement *>()));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(FunctionCall)
		{
			statement * s, *p;
			vector<statement *> params;
			// FunctionCall(test, Params())
			s = parse_statement("test()");
			p = new function_call("test", vector<statement *>());
			Assert::AreEqual(s->parse_string(), p->parse_string());
			// FunctionCall(test, Params(Value("Hello World")))
			s = parse_statement("test(\"Hello World\")");
			params = {
				new value("\"Hello World\"")
			};
			p = new function_call("test", params);
			Assert::AreEqual(s->parse_string(), p->parse_string());
			// FunctionCall(test, Params(Value("Hello World"),Value(15),Statement(x)))
			s = parse_statement("test(\"Hello World\", 15, x)");
			params = {
				new value("\"Hello World\""),
				new value("15"),
				new statement("x")
			};
			p = new function_call("test", params);
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(FunctionDef)
		{
			statement * s, *p;
			vector<function::function_parameter> params;
			s = parse_statement("def test(): Some");
			p = new function_heading("test", vector<function::function_parameter>(),
				type_class("Some"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("def test(arg: String): Some");
			params = {
				{ type_class("String"), "arg" }
			};
			p = new function_heading("test", params, type_class("Some"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("def test(argc: Int, argv: String): Some");
			params = {
				{ type_class("Int"), "argc" },
				{ type_class("String"), "argv" }
			};
			p = new function_heading("test", params, type_class("Some"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("def test(argc: Int, argv: String)");
			params = {
				{ type_class("Int"), "argc" },
				{ type_class("String"), "argv" }
			};
			p = new function_heading("test", params, type_class(""));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(ClassDef)
		{
			statement * s, *p;
			vector<class_heading::heading_generic> gt;
			s = parse_statement("class Test");
			p = new class_heading(class_heading::class_type::CLASS, "Test");
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("class Test<T>");
			gt.push_back(class_heading::generate_generic_heading("T", type_class("")));
			p = new class_heading(class_heading::class_type::CLASS, "Test", gt);
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("class Test<T> : Parent");
			gt = vector<class_heading::heading_generic>();
			gt.push_back(class_heading::generate_generic_heading("T", type_class("")));
			p = new class_heading(class_heading::class_type::CLASS, "Test", gt, 
				type_class("Parent"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("class Test<T:Extends> : Parent");
			gt = vector<class_heading::heading_generic>();
			gt.push_back(class_heading::generate_generic_heading("T", type_class("Extends")));
			p = new class_heading(class_heading::class_type::CLASS, "Test", gt,
				type_class("Parent"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(StructDef)
		{
			statement * s, *p;
			vector<class_heading::heading_generic> gt;
			s = parse_statement("struct Test");
			p = new class_heading(class_heading::class_type::STRUCT, "Test");
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("struct Test<T>");
			gt.push_back(class_heading::generate_generic_heading("T", type_class("")));
			p = new class_heading(class_heading::class_type::STRUCT, "Test", gt);
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("struct Test<T> : Parent");
			gt = vector<class_heading::heading_generic>();
			gt.push_back(class_heading::generate_generic_heading("T", type_class("")));
			p = new class_heading(class_heading::class_type::STRUCT, "Test", gt,
				type_class("Parent"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("struct Test<T:Extends> : Parent");
			gt = vector<class_heading::heading_generic>();
			gt.push_back(class_heading::generate_generic_heading("T", type_class("Extends")));
			p = new class_heading(class_heading::class_type::STRUCT, "Test", gt,
				type_class("Parent"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(InstanceClass)
		{
			statement * s, *p;
			vector<statement *> params;
			s = parse_statement("new Test()");
			p = new new_object_statement(type_class("Test"), vector<statement *>());
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("new Test(x)");
			params = {
				new statement("x")
			};
			p = new new_object_statement(type_class("Test"), params);
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("new Test(5, x)");
			params = {
				new value("5"),
				new statement("x")
			};
			p = new new_object_statement(type_class("Test"), params);
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(ReturnStatement)
		{
			statement * s = parse_statement("return x");
			statement * p = new return_statement(new statement("x"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(Literals)
		{
			statement * s, *p;
			s = parse_statement("\"Hello, World!\"");
			p = new value("\"Hello, World!\"");
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("'Hello, World!'");
			p = new value("'Hello, World!'");
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("true");
			p = new value("true");
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("false");
			p = new value("false");
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("5");
			p = new value("5");
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("5.5");
			p = new value("5.5");
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(Bitwise)
		{
			statement * s, *p;
			s = parse_statement("x & y");
			p = new bitwise(new statement("x"), bitwise::operation::And, new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x | y");
			p = new bitwise(new statement("x"), bitwise::operation::Or, new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x ^ y");
			p = new bitwise(new statement("x"), bitwise::operation::Xor, new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x << 5");
			p = new bitwise(new statement("x"), bitwise::operation::ShiftLeft, new value("5"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x >> 5");
			p = new bitwise(new statement("x"), bitwise::operation::ShiftRight, new value("5"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("~x");
			p = new bitinvert(new statement("x"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(BooleanOperations)
		{
			statement * s, *p;
			s = parse_statement("x && y");
			p = new boolean_conjunction(new statement("x"),
				boolean_conjunction::conjunction_type::And, new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x || y");
			p = new boolean_conjunction(new statement("x"),
				boolean_conjunction::conjunction_type::Or, new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("!x");
			p = new invert(new statement("x"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}


		TEST_METHOD(Comparison)
		{
			statement * s, *p;
			s = parse_statement("x == y");
			p = new comparison(new statement("x"), comparison::comparison_type::Equal,
				new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x < y");
			p = new comparison(new statement("x"), comparison::comparison_type::LessThan,
				new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x > y");
			p = new comparison(new statement("x"), comparison::comparison_type::GreaterThan,
				new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x <= y");
			p = new comparison(new statement("x"), comparison::comparison_type::LessThanEqualTo,
				new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x >= y");
			p = new comparison(new statement("x"), comparison::comparison_type::GreaterThanEqualTo,
				new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x != y");
			p = new comparison(new statement("x"), comparison::comparison_type::NotEqual,
				new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(BasicMath)
		{
			statement * s, *p;
			queue<statement *> operands;
			queue<char> operators;
			vector<char> basics = { '+', '-', '*', '/', '%' };
			for (char c : basics)
			{
				s = parse_statement("x " + string(1, c) + " y");
				operands = queue<statement *>();
				operands.push(new statement("x"));
				operands.push(new statement("y"));
				operators = queue<char>();
				operators.push(c);
				p = new math_statement(operands, operators);
				Assert::AreEqual(s->parse_string(), p->parse_string());
				s = parse_statement("x " + string(1, c) + "= y");
				operands = queue<statement *>();
				operands.push(new statement("x"));
				operands.push(new statement("y"));
				operators = queue<char>();
				operators.push(c);
				p = new assignment(new statement("x"), new math_statement(operands, operators));
				Assert::AreEqual(s->parse_string(), p->parse_string());
			}
			s = parse_statement("x++");
			p = new self_modifier(self_modifier::modifier_type::PLUS, 
				self_modifier::modifier_time::POST, new statement("x"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("++x");
			p = new self_modifier(self_modifier::modifier_type::PLUS,
				self_modifier::modifier_time::PRE, new statement("x"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x--");
			p = new self_modifier(self_modifier::modifier_type::MINUS,
				self_modifier::modifier_time::POST, new statement("x"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("--x");
			p = new self_modifier(self_modifier::modifier_type::MINUS,
				self_modifier::modifier_time::PRE, new statement("x"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(ListOperations)
		{
			statement * s, *p;
			s = parse_statement("x[y]");
			p = new list_accessor(new statement("x"), new statement("y"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("x[y] = z");
			p = new assignment(new list_accessor(new statement("x"), new statement("y")), 
				new statement("z"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(IfStatement)
		{
			statement * s, *p;
			s = parse_statement("if(x)");
			p = new if_heading(new statement("x"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("if(x == y)");
			p = new if_heading(new comparison(new statement("x"), 
				comparison::comparison_type::Equal, new statement("y")));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(WhileStatement)
		{
			statement * s, *p;
			s = parse_statement("while(x)");
			p = new while_heading(new statement("x"));
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("while(x == y)");
			p = new while_heading(new comparison(new statement("x"),
				comparison::comparison_type::Equal, new statement("y")));
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(ForStatement)
		{
			statement * s, *p;
			s = parse_statement("for(x: Int = 0; x < 10; x++)");
			p = new for_classic_heading(
				new decleration_with_assignment(
					new statement("x"), 
					type_class("Int"), 
					new value("0")
				), 
				new comparison(
					new statement("x"), 
					comparison::comparison_type::LessThan, 
					new value("10")
				), 
				new self_modifier(
					self_modifier::modifier_type::PLUS, 
					self_modifier::modifier_time::POST,
					new statement("x")
				)
			);
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("for(x: Int : lst)");
			p = new for_itterator_heading(
				new decleration("x", type_class("Int")), 
				new statement("lst")
			);
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}

		TEST_METHOD(FlowControls)
		{
			statement * s, *p;
			s = parse_statement("break");
			p = new flow_statement(flow_statement::type::BREAK);
			Assert::AreEqual(s->parse_string(), p->parse_string());
			s = parse_statement("next");
			p = new flow_statement(flow_statement::type::NEXT);
			Assert::AreEqual(s->parse_string(), p->parse_string());
		}
	};
}