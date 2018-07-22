#include "../../Core/statement.h"
#include "../../Core/parser.h"
#include "../../Core/utils.h"
#include "../test_util.h"
#include <string>
#include <vector>
#include <queue>
#include <iostream>

using std::string;
using std::vector;
using std::queue;

using skiff::parser;
using skiff::tokenizer::tokenize;

using namespace skiff::statements;

namespace Test
{
	TEST_CLASS(Parsing, 3)
	{
		TEST_METHOD(Declaration)
		{
			// Decleration(x,TypeClass(Int))
			statement * s, *p;

			s = parser(tokenize("x: Int")).parse().at(0);
			p = new declaration("x", type_statement("Int"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

//			s = parser(tokenize("x: List<Type>");
//			vector<type_statement> et;
//			et.push_back(type_statement("Type"));
//			p = new declaration("x", type_statement("List", et));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(Assignment)
		{
			// Assignment(Statement(x),Value(5))
			statement * s = parser(tokenize("x = 5")).parse().at(0);
			statement * p = new assignment(
				new variable("x"), new value("5"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(AssignmentAndDeclaration)
		{
			// DeclareAndAssign(Statement(x), TypeClass(Int), Value(5))
			statement * s, *p;

			s = parser(tokenize("x: Int = 5")).parse().at(0);
			p = new declaration_with_assignment(
				"x", type_statement("Int"),
				new value("5"));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = parser(tokenize("x: List<Type> = new List<Type>()")).parse().at(0);
			vector<type_statement> et;
			et.push_back(type_statement("Type"));
			p = new declaration_with_assignment(
				"x", type_statement("List", et),
				new new_object_statement(
					type_statement("List", et),
					vector<statement *>()));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(FunctionCall)
		{
			statement * s, *p;
			vector<statement *> params;

			// FunctionCall(test, Params())
			s = parser(tokenize("test()")).parse().at(0);
			p = new function_call(new variable("test"), vector<statement *>());
			Assert::AreEqual(p->parse_string(), s->parse_string());


			// FunctionCall(test, Params(Value("Hello World")))
			s = parser(tokenize("test(\"Hello World\")")).parse().at(0);
			params = {
				new value("\"Hello World\"")
			};
			p = new function_call(new variable("test"), params);
			Assert::AreEqual(p->parse_string(), s->parse_string());


			// FunctionCall(test, Params(Value("Hello World"),Value(15),Statement(x)))
			s = parser(tokenize("test(\"Hello World\", 15, x)")).parse().at(0);
			params = {
				new value("\"Hello World\""),
				new value("15"),
				new variable("x")
			};
			p = new function_call(new variable("test"), params);
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(FunctionDef)
		{
//			statement * s, *p;
//			vector<function_heading::function_parameter> params;
//
//			s = parser(tokenize("def test(): Some")).parse().at(0);
//			p = new function_heading("test",
//				vector<function_heading::function_parameter>(),
//				type_statement("Some"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("def test(arg: String): Some")).parse().at(0);
//			params = {
//				{ type_statement("String"), "arg" }
//			};
//			p = new function_heading("test", params,
//				type_statement("Some"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("def test(argc: Int, argv: String): Some")).parse().at(0);
//			params = {
//				{ type_statement("Int"), "argc" },
//				{ type_statement("String"), "argv" }
//			};
//			p = new function_heading("test", params,
//				type_statement("Some"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("def test(argc: Int, argv: String)")).parse().at(0);
//			params = {
//				{ type_statement("Int"), "argc" },
//				{ type_statement("String"), "argv" }
//			};
//			p = new function_heading("test", params,
//				type_statement(""));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(ClassDef)
		{
			statement * s, *p;
			vector<class_heading::heading_generic> gt;

			s = parser(tokenize("class Test")).parse().at(0);
			p = new class_heading(
				class_heading::class_type::CLASS, "Test");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("class Test<T>")).parse().at(0);
			gt.push_back(
				class_heading::generate_generic_heading("T",
					type_statement("")));
			p = new class_heading(
				class_heading::class_type::CLASS, "Test", gt);
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("class Test<T> : Parent")).parse().at(0);
			gt = vector<class_heading::heading_generic>();
			gt.push_back(
				class_heading::generate_generic_heading("T",
					type_statement("")));
			p = new class_heading(
				class_heading::class_type::CLASS, "Test", gt,
				type_statement("Parent"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("class Test<T:Extends> : Parent")).parse().at(0);
			gt = vector<class_heading::heading_generic>();
			gt.push_back(
				class_heading::generate_generic_heading("T",
					type_statement("Extends")));
			p = new class_heading(
				class_heading::class_type::CLASS, "Test", gt,
				type_statement("Parent"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(StructDef)
		{
			statement * s, *p;
			vector<class_heading::heading_generic> gt;

			s = parser(tokenize("struct Test")).parse().at(0);
			p = new class_heading(
				class_heading::class_type::STRUCT, "Test");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("struct Test<T>")).parse().at(0);
			gt.push_back(
				class_heading::generate_generic_heading("T",
					type_statement("")));
			p = new class_heading(
				class_heading::class_type::STRUCT, "Test", gt);
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("struct Test<T> : Parent")).parse().at(0);
			gt = vector<class_heading::heading_generic>();
			gt.push_back(
				class_heading::generate_generic_heading("T",
					type_statement("")));
			p = new class_heading(
				class_heading::class_type::STRUCT, "Test", gt,
				type_statement("Parent"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("struct Test<T:Extends> : Parent")).parse().at(0);
			gt = vector<class_heading::heading_generic>();
			gt.push_back(
				class_heading::generate_generic_heading("T",
					type_statement("Extends")));
			p = new class_heading(
				class_heading::class_type::STRUCT, "Test", gt,
				type_statement("Parent"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(InstanceClass)
		{
			statement * s, *p;
			vector<statement *> params;

			s = parser(tokenize("new Test()")).parse().at(0);
			p = new new_object_statement(type_statement("Test"),
				vector<statement *>());
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = parser(tokenize("new Test(x)")).parse().at(0);
			params = {
				new variable("x")
			};
			p = new new_object_statement(type_statement("Test"),
				params);
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = parser(tokenize("new Test(5, x)")).parse().at(0);
			params = {
				new value("5"),
				new variable("x")
			};
			p = new new_object_statement(type_statement("Test"),
				params);
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(ReturnStatement)
		{
			statement * s = parser(tokenize("return x")).parse().at(0);
			statement * p = new return_statement(
				new variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(Literals)
		{
			statement * s, *p;
			s = parser(tokenize("\"Hello, World!\"")).parse().at(0);
			p = new value("\"Hello, World!\"");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("'Hello, World!'")).parse().at(0);
			p = new value("'Hello, World!'");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("true")).parse().at(0);
			p = new value("true");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("false")).parse().at(0);
			p = new value("false");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("5")).parse().at(0);
			p = new value("5");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("5.5")).parse().at(0);
			p = new value("5.5");
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(Bitwise)
		{
			statement * s, *p;

			s = parser(tokenize("x & y")).parse().at(0);
			p = new bitwise(new variable("x"),
				bitwise::operation::AND, new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x | y")).parse().at(0);
			p = new bitwise(new variable("x"),
				bitwise::operation::OR, new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x ^ y")).parse().at(0);
			p = new bitwise(new variable("x"),
				bitwise::operation::XOR, new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x << 5")).parse().at(0);
			p = new bitwise(new variable("x"),
				bitwise::operation::SHIFT_LEFT, new value("5"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x >> 5")).parse().at(0);
			p = new bitwise(new variable("x"),
				bitwise::operation::SHIFT_RIGHT, new value("5"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("~x")).parse().at(0);
			p = new bitinvert(new variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(BooleanOperations)
		{
			statement * s, *p;

			s = parser(tokenize("x && y")).parse().at(0);
			p = new boolean_conjunction(new variable("x"),
				boolean_conjunction::conjunction_type::AND,
				new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x || y")).parse().at(0);
			p = new boolean_conjunction(new variable("x"),
				boolean_conjunction::conjunction_type::AND,
				new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("!x")).parse().at(0);
			p = new invert(new variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}


		TEST_METHOD(Comparison)
		{
			statement * s, *p;

			s = parser(tokenize("x == y")).parse().at(0);
			p = new comparison(new variable("x"),
				comparison::comparison_type::EQUAL,
				new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x < y")).parse().at(0);
			p = new comparison(new variable("x"),
				comparison::comparison_type::LESS_THAN,
				new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x > y")).parse().at(0);
			p = new comparison(new variable("x"),
				comparison::comparison_type::GREATER_THAN,
				new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x <= y")).parse().at(0);
			p = new comparison(new variable("x"),
				comparison::comparison_type::LESS_THAN_EQUAL_TO,
				new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x >= y")).parse().at(0);
			p = new comparison(new variable("x"),
				comparison::comparison_type::GREATER_THAN_EQUAL_TO,
				new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x != y")).parse().at(0);
			p = new comparison(new variable("x"),
				comparison::comparison_type::NOT_EQUAL,
				new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(BasicMath)
		{
//			statement * s, *p;
//			queue<statement *> operands;
//			queue<char> operators;
//
//			vector<char> basics = { '+', '-', '*', '/', '%' };
//			for (char c : basics)
//			{
//				s = parser(tokenize("x " + string(1, c) + " y");
//				operands = queue<statement *>();
//				operands.push(new variable("x"));
//				operands.push(new variable("y"));
//				operators = queue<char>();
//				operators.push(c);
//				p = new math_statement(operands, operators);
//				Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//				s = parser(tokenize("x " + string(1, c) + "= y");
//				operands = queue<statement *>();
//				operands.push(new variable("x"));
//				operands.push(new variable("y"));
//				operators = queue<char>();
//				operators.push(c);
//				p = new assignment(new variable("x"),
//					new math_statement(operands, operators));
//				Assert::AreEqual(p->parse_string(), s->parse_string());
//			}
//
//			s = parser(tokenize("x++");
//			p = new self_modifier(
//				self_modifier::modifier_type::PLUS,
//				self_modifier::modifier_time::POST,
//				new variable("x"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("++x");
//			p = new self_modifier(
//				self_modifier::modifier_type::PLUS,
//				self_modifier::modifier_time::PRE,
//				new variable("x"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("x--");
//			p = new self_modifier(
//				self_modifier::modifier_type::MINUS,
//				self_modifier::modifier_time::POST,
//				new variable("x"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("--x");
//			p = new self_modifier(
//				self_modifier::modifier_type::MINUS,
//				self_modifier::modifier_time::PRE,
//				new variable("x"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(ListOperations)
		{
			statement * s, *p;

			s = parser(tokenize("x[y]")).parse().at(0);
			p = new list_accessor(
				new variable("x"), new variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("x[y] = z")).parse().at(0);
			p = new assignment(
				new list_accessor(
					new variable("x"),
					new variable("y")),
				new variable("z"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(IfStatement)
		{
//			statement * s, *p;
//
//			s = parser(tokenize("if(x)")).parse().at(0);
//			p = new if_heading(new variable("x"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//			s = parser(tokenize("if(x == y)")).parse().at(0);
//			p = new if_heading(new comparison(
//				new variable("x"),
//				comparison::comparison_type::Equal,
//				new variable("y")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(WhileStatement)
		{
//			statement * s, *p;
//
//			s = parser(tokenize("while(x)")).parse().at(0);
//			p = new while_heading(new variable("x"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//			s = parser(tokenize("while(x == y)")).parse().at(0);
//			p = new while_heading(new comparison(
//				new variable("x"),
//				comparison::comparison_type::Equal,
//				new variable("y")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(ForStatement)
		{
//			statement * s, *p;
//
//			s = parser(tokenize("for(x: Int = 0; x < 10; x++)")).parse().at(0);
//			p = new for_classic_heading(
//				new declaration_with_assignment(
//					"x",
//					type_statement("Int"),
//					new value("0")
//				),
//				new comparison(
//					new variable("x"),
//					comparison::comparison_type::LessThan,
//					new value("10")
//				),
//				new self_modifier(
//					self_modifier::modifier_type::PLUS,
//					self_modifier::modifier_time::POST,
//					new variable("x")
//				)
//			);
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//			s = parser(tokenize("for(x: Int : lst)")).parse().at(0);
//			p = new for_itterator_heading(
//				new declaration("x", type_statement("Int")),
//				new variable("lst")
//			);
//			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(FlowControls)
		{
			statement * s, *p;

			s = parser(tokenize("break")).parse().at(0);
			p = new flow_statement(
				flow_statement::type::BREAK);
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("next")).parse().at(0);
			p = new flow_statement(
				flow_statement::type::NEXT);
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(ImportStatement)
		{
			statement * s, *p;

			s = parser(tokenize("import \"localfile\"")).parse().at(0);
			p = new import_statement("\"localfile\"");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = parser(tokenize("import <extenfile>")).parse().at(0);
			p = new import_statement("<extenfile>");
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(DeclarationModifiers)
		{
//			statement * s, *p;
//
//			s = parser(tokenize("static def test()")).parse().at(0);
//			p = new modifier(
//				modifier::modifier_type::STATIC,
//				new function_heading("test"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("private def test()")).parse().at(0);
//			p = new modifier(
//				modifier::modifier_type::PRIVATE,
//				new function_heading("test"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("private static def test()")).parse().at(0);
//			p = new modifier(
//				modifier::modifier_type::PRIVATE,
//				new modifier(
//					modifier::modifier_type::STATIC,
//					new function_heading("test")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("static test: String")).parse().at(0);
//			p = new modifier(modifier::modifier_type::STATIC,
//				new declaration("test", type_statement("String")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("private test: String")).parse().at(0);
//			p = new modifier(modifier::modifier_type::PRIVATE,
//				new declaration("test", type_statement("String")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("private static test: String")).parse().at(0);
//			p = new modifier(modifier::modifier_type::PRIVATE,
//				new modifier(modifier::modifier_type::STATIC,
//					new declaration("test", type_statement("String"))
//				));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(Annotation)
		{
//			statement * s, *p;
//			vector<statement *> anno_params;
//			vector<function_heading::function_parameter> func_params;
//
//			s = parser(tokenize("@Anno def test()")).parse().at(0);
//			p = new annotation_tag("Anno", anno_params,
//				new function_heading("test", func_params,
//					type_statement("")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("@Anno(param) def test()")).parse().at(0);
//			anno_params = vector<statement *>();
//			anno_params.push_back(new variable("param"));
//			p = new annotation_tag("Anno", anno_params,
//				new function_heading("test", func_params,
//					type_statement("")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("@Anno(param, paramz) def test()")).parse().at(0);
//			anno_params = vector<statement *>();
//			anno_params.push_back(new variable("param"));
//			anno_params.push_back(new variable("paramz"));
//			p = new annotation_tag("Anno", anno_params,
//				new function_heading("test", func_params,
//					type_statement("")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("@Anno def test(argz: String): Int")).parse().at(0);
//			func_params = vector<function_heading::function_parameter>();
//			func_params.push_back(
//				function_heading::create_function_parameter("argz",
//					type_statement("String"))
//			);
//			p = new annotation_tag("Anno", anno_params,
//				new function_heading("test", func_params,
//					type_statement("Int")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("@Anno(param) def test(argz: String): Int")).parse().at(0);
//			anno_params = vector<statement *>();
//			anno_params.push_back(new variable("param"));
//			func_params = vector<function_heading::function_parameter>();
//			func_params.push_back(
//				function_heading::create_function_parameter("argz",
//					type_statement("String"))
//			);
//			p = new annotation_tag("Anno", anno_params,
//				new function_heading("test", func_params,
//					type_statement("Int")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//
//			s = parser(tokenize("@Anno(param, paramz) def test(argz: String, a: Char): Int")).parse().at(0);
//			anno_params = vector<statement *>();
//			anno_params.push_back(new variable("param"));
//			anno_params.push_back(new variable("paramz"));
//			func_params = vector<function_heading::function_parameter>();
//			func_params.push_back(
//				function_heading::create_function_parameter("argz",
//					type_statement("String"))
//			);
//			func_params.push_back(
//				function_heading::create_function_parameter("a",
//					type_statement("Char"))
//			);
//			p = new annotation_tag("Anno", anno_params,
//				new function_heading("test", func_params,
//					type_statement("Int")));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(SwitchMatchHeading)
		{
//			statement * s, *p;
//			vector<string> struct_params;
//
//			s = parser(tokenize("switch(x)")).parse().at(0);
//			p = new switch_heading(
//				switch_heading::type::SWITCH,
//				new variable("x"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//			s = parser(tokenize("match(x)")).parse().at(0);
//			p = new switch_heading(
//				switch_heading::type::MATCH,
//				new variable("x"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//			s = parser(tokenize("case \"test\" =>")).parse().at(0);
//			p = new switch_case_heading(
//				new value("\"test\""));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//			s = parser(tokenize("case 5 =>")).parse().at(0);
//			p = new switch_case_heading(new value("5"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//			s = parser(tokenize("case val: ClassClass =>")).parse().at(0);
//			p = new match_case_heading("val",
//				type_statement("ClassClass"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//			struct_params = vector<string>();
//			struct_params = { "p1", "p2" };
//			s = parser(tokenize("case val: StructClass(p1, p2) =>")).parse().at(0);
//			p = new match_case_heading("val",
//				type_statement("StructClass"), struct_params);
//			Assert::AreEqual(p->parse_string(), s->parse_string());
//
//			s = parser(tokenize("case _ =>")).parse().at(0);
//			p = new switch_case_heading(new variable("_"));
//			Assert::AreEqual(p->parse_string(), s->parse_string());
		}
	};
}