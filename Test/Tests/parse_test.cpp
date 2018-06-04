#include "../../Core/statement.h"
#include "../../Core/parsers.h"
#include "../../Core/utils.h"
#include "../test_util.h"
#include <string>
#include <vector>
#include <queue>
#include <iostream>

using std::string;
using std::vector;
using std::queue;

namespace Test
{
	TEST_CLASS(Parsing, 2)
	{
		TEST_METHOD(Declaration)
		{
			// Decleration(x,TypeClass(Int))
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("x: Int");
			p = new skiff::statements::decleration("x", skiff::statements::type_statement("Int"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x: List<Type>");
			vector<skiff::statements::type_statement> et;
			et.push_back(skiff::statements::type_statement("Type"));
			p = new skiff::statements::decleration("x", skiff::statements::type_statement("List", et));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(Assignment)
		{
			// Assignment(Statement(x),Value(5))
			skiff::statements::statement * s = skiff::parse_statement("x = 5");
			skiff::statements::statement * p = new skiff::statements::assignment(
				new skiff::statements::variable("x"), new skiff::statements::value("5"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(AssignmentAndDeclaration)
		{
			// DeclareAndAssign(Statement(x), TypeClass(Int), Value(5))
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("x: Int = 5");
			p = new skiff::statements::decleration_with_assignment(
				"x", skiff::statements::type_statement("Int"), 
				new skiff::statements::value("5"));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("x: List<Type> = new List<Type>()");
			vector<skiff::statements::type_statement> et;
			et.push_back(skiff::statements::type_statement("Type"));
			p = new skiff::statements::decleration_with_assignment(
				"x", skiff::statements::type_statement("List", et), 
				new skiff::statements::new_object_statement(
					skiff::statements::type_statement("List", et), 
					vector<skiff::statements::statement *>()));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(FunctionCall)
		{
			skiff::statements::statement * s, *p;
			vector<skiff::statements::statement *> params;

			// FunctionCall(test, Params())
			s = skiff::parse_statement("test()");
			p = new skiff::statements::function_call("test", 
				vector<skiff::statements::statement *>());
			Assert::AreEqual(p->parse_string(), s->parse_string());


			// FunctionCall(test, Params(Value("Hello World")))
			s = skiff::parse_statement("test(\"Hello World\")");
			params = {
				new skiff::statements::value("\"Hello World\"")
			};
			p = new skiff::statements::function_call("test", params);
			Assert::AreEqual(p->parse_string(), s->parse_string());


			// FunctionCall(test, Params(Value("Hello World"),Value(15),Statement(x)))
			s = skiff::parse_statement("test(\"Hello World\", 15, x)");
			params = {
				new skiff::statements::value("\"Hello World\""),
				new skiff::statements::value("15"),
				new skiff::statements::variable("x")
			};
			p = new skiff::statements::function_call("test", params);
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(FunctionDef)
		{
			skiff::statements::statement * s, *p;
			vector<skiff::statements::function_heading::function_parameter> params;

			s = skiff::parse_statement("def test(): Some");
			p = new skiff::statements::function_heading("test", 
				vector<skiff::statements::function_heading::function_parameter>(),
				skiff::statements::type_statement("Some"));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("def test(arg: String): Some");
			params = {
				{ skiff::statements::type_statement("String"), "arg" }
			};
			p = new skiff::statements::function_heading("test", params, 
				skiff::statements::type_statement("Some"));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("def test(argc: Int, argv: String): Some");
			params = {
				{ skiff::statements::type_statement("Int"), "argc" },
				{ skiff::statements::type_statement("String"), "argv" }
			};
			p = new skiff::statements::function_heading("test", params, 
				skiff::statements::type_statement("Some"));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("def test(argc: Int, argv: String)");
			params = {
				{ skiff::statements::type_statement("Int"), "argc" },
				{ skiff::statements::type_statement("String"), "argv" }
			};
			p = new skiff::statements::function_heading("test", params, 
				skiff::statements::type_statement(""));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(ClassDef)
		{
			skiff::statements::statement * s, *p;
			vector<skiff::statements::class_heading::heading_generic> gt;

			s = skiff::parse_statement("class Test");
			p = new skiff::statements::class_heading(
				skiff::statements::class_heading::class_type::CLASS, "Test");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("class Test<T>");
			gt.push_back(
				skiff::statements::class_heading::generate_generic_heading("T", 
					skiff::statements::type_statement("")));
			p = new skiff::statements::class_heading(
				skiff::statements::class_heading::class_type::CLASS, "Test", gt);
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("class Test<T> : Parent");
			gt = vector<skiff::statements::class_heading::heading_generic>();
			gt.push_back(
				skiff::statements::class_heading::generate_generic_heading("T", 
					skiff::statements::type_statement("")));
			p = new skiff::statements::class_heading(
				skiff::statements::class_heading::class_type::CLASS, "Test", gt, 
				skiff::statements::type_statement("Parent"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("class Test<T:Extends> : Parent");
			gt = vector<skiff::statements::class_heading::heading_generic>();
			gt.push_back(
				skiff::statements::class_heading::generate_generic_heading("T", 
					skiff::statements::type_statement("Extends")));
			p = new skiff::statements::class_heading(
				skiff::statements::class_heading::class_type::CLASS, "Test", gt,
				skiff::statements::type_statement("Parent"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(StructDef)
		{
			skiff::statements::statement * s, *p;
			vector<skiff::statements::class_heading::heading_generic> gt;

			s = skiff::parse_statement("struct Test");
			p = new skiff::statements::class_heading(
				skiff::statements::class_heading::class_type::STRUCT, "Test");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("struct Test<T>");
			gt.push_back(
				skiff::statements::class_heading::generate_generic_heading("T", 
					skiff::statements::type_statement("")));
			p = new skiff::statements::class_heading(
				skiff::statements::class_heading::class_type::STRUCT, "Test", gt);
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("struct Test<T> : Parent");
			gt = vector<skiff::statements::class_heading::heading_generic>();
			gt.push_back(
				skiff::statements::class_heading::generate_generic_heading("T", 
					skiff::statements::type_statement("")));
			p = new skiff::statements::class_heading(
				skiff::statements::class_heading::class_type::STRUCT, "Test", gt,
				skiff::statements::type_statement("Parent"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("struct Test<T:Extends> : Parent");
			gt = vector<skiff::statements::class_heading::heading_generic>();
			gt.push_back(
				skiff::statements::class_heading::generate_generic_heading("T", 
					skiff::statements::type_statement("Extends")));
			p = new skiff::statements::class_heading(
				skiff::statements::class_heading::class_type::STRUCT, "Test", gt,
				skiff::statements::type_statement("Parent"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(InstanceClass)
		{
			skiff::statements::statement * s, *p;
			vector<skiff::statements::statement *> params;

			s = skiff::parse_statement("new Test()");
			p = new skiff::statements::new_object_statement(skiff::statements::type_statement("Test"), 
				vector<skiff::statements::statement *>());
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("new Test(x)");
			params = {
				new skiff::statements::variable("x")
			};
			p = new skiff::statements::new_object_statement(skiff::statements::type_statement("Test"), 
				params);
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("new Test(5, x)");
			params = {
				new skiff::statements::value("5"),
				new skiff::statements::variable("x")
			};
			p = new skiff::statements::new_object_statement(skiff::statements::type_statement("Test"), 
				params);
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(ReturnStatement)
		{
			skiff::statements::statement * s = skiff::parse_statement("return x");
			skiff::statements::statement * p = new skiff::statements::return_statement(
				new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(Literals)
		{
			skiff::statements::statement * s, *p;
			s = skiff::parse_statement("\"Hello, World!\"");
			p = new skiff::statements::value("\"Hello, World!\"");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("'Hello, World!'");
			p = new skiff::statements::value("'Hello, World!'");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("true");
			p = new skiff::statements::value("true");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("false");
			p = new skiff::statements::value("false");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("5");
			p = new skiff::statements::value("5");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("5.5");
			p = new skiff::statements::value("5.5");
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(Bitwise)
		{
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("x & y");
			p = new skiff::statements::bitwise(new skiff::statements::variable("x"), 
				skiff::statements::bitwise::operation::And, new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x | y");
			p = new skiff::statements::bitwise(new skiff::statements::variable("x"), 
				skiff::statements::bitwise::operation::Or, new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x ^ y");
			p = new skiff::statements::bitwise(new skiff::statements::variable("x"), 
				skiff::statements::bitwise::operation::Xor, new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x << 5");
			p = new skiff::statements::bitwise(new skiff::statements::variable("x"), 
				skiff::statements::bitwise::operation::ShiftLeft, new skiff::statements::value("5"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x >> 5");
			p = new skiff::statements::bitwise(new skiff::statements::variable("x"),
				skiff::statements::bitwise::operation::ShiftRight, new skiff::statements::value("5"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("~x");
			p = new skiff::statements::bitinvert(new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(BooleanOperations)
		{
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("x && y");
			p = new skiff::statements::boolean_conjunction(new skiff::statements::variable("x"),
				skiff::statements::boolean_conjunction::conjunction_type::And, 
				new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x || y");
			p = new skiff::statements::boolean_conjunction(new skiff::statements::variable("x"),
				skiff::statements::boolean_conjunction::conjunction_type::Or, 
				new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("!x");
			p = new skiff::statements::invert(new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}


		TEST_METHOD(Comparison)
		{
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("x == y");
			p = new skiff::statements::comparison(new skiff::statements::variable("x"), 
				skiff::statements::comparison::comparison_type::Equal,
				new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x < y");
			p = new skiff::statements::comparison(new skiff::statements::variable("x"), 
				skiff::statements::comparison::comparison_type::LessThan,
				new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x > y");
			p = new skiff::statements::comparison(new skiff::statements::variable("x"), 
				skiff::statements::comparison::comparison_type::GreaterThan,
				new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x <= y");
			p = new skiff::statements::comparison(new skiff::statements::variable("x"), 
				skiff::statements::comparison::comparison_type::LessThanEqualTo,
				new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x >= y");
			p = new skiff::statements::comparison(new skiff::statements::variable("x"),
				skiff::statements::comparison::comparison_type::GreaterThanEqualTo,
				new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x != y");
			p = new skiff::statements::comparison(new skiff::statements::variable("x"), 
				skiff::statements::comparison::comparison_type::NotEqual,
				new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(BasicMath)
		{
			skiff::statements::statement * s, *p;
			queue<skiff::statements::statement *> operands;
			queue<char> operators;

			vector<char> basics = { '+', '-', '*', '/', '%' };
			for (char c : basics)
			{
				s = skiff::parse_statement("x " + string(1, c) + " y");
				operands = queue<skiff::statements::statement *>();
				operands.push(new skiff::statements::variable("x"));
				operands.push(new skiff::statements::variable("y"));
				operators = queue<char>();
				operators.push(c);
				p = new skiff::statements::math_statement(operands, operators);
				Assert::AreEqual(p->parse_string(), s->parse_string());


				s = skiff::parse_statement("x " + string(1, c) + "= y");
				operands = queue<skiff::statements::statement *>();
				operands.push(new skiff::statements::variable("x"));
				operands.push(new skiff::statements::variable("y"));
				operators = queue<char>();
				operators.push(c);
				p = new skiff::statements::assignment(new skiff::statements::variable("x"), 
					new skiff::statements::math_statement(operands, operators));
				Assert::AreEqual(p->parse_string(), s->parse_string());
			}

			s = skiff::parse_statement("x++");
			p = new skiff::statements::self_modifier(
				skiff::statements::self_modifier::modifier_type::PLUS,
				skiff::statements::self_modifier::modifier_time::POST, 
				new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("++x");
			p = new skiff::statements::self_modifier(
				skiff::statements::self_modifier::modifier_type::PLUS,
				skiff::statements::self_modifier::modifier_time::PRE, 
				new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("x--");
			p = new skiff::statements::self_modifier(
				skiff::statements::self_modifier::modifier_type::MINUS,
				skiff::statements::self_modifier::modifier_time::POST, 
				new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("--x");
			p = new skiff::statements::self_modifier(
				skiff::statements::self_modifier::modifier_type::MINUS,
				skiff::statements::self_modifier::modifier_time::PRE, 
				new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(ListOperations)
		{
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("x[y]");
			p = new skiff::statements::list_accessor(
				new skiff::statements::variable("x"), new skiff::statements::variable("y"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("x[y] = z");
			p = new skiff::statements::assignment(
				new skiff::statements::list_accessor(
					new skiff::statements::variable("x"), 
					new skiff::statements::variable("y")), 
				new skiff::statements::variable("z"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(IfStatement)
		{
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("if(x)");
			p = new skiff::statements::if_heading(new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("if(x == y)");
			p = new skiff::statements::if_heading(new skiff::statements::comparison(
				new skiff::statements::variable("x"), 
				skiff::statements::comparison::comparison_type::Equal, 
				new skiff::statements::variable("y")));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(WhileStatement)
		{
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("while(x)");
			p = new skiff::statements::while_heading(new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("while(x == y)");
			p = new skiff::statements::while_heading(new skiff::statements::comparison(
				new skiff::statements::variable("x"),
				skiff::statements::comparison::comparison_type::Equal, 
				new skiff::statements::variable("y")));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(ForStatement)
		{
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("for(x: Int = 0; x < 10; x++)");
			p = new skiff::statements::for_classic_heading(
				new skiff::statements::decleration_with_assignment(
					"x", 
					skiff::statements::type_statement("Int"), 
					new skiff::statements::value("0")
				), 
				new skiff::statements::comparison(
					new skiff::statements::variable("x"), 
					skiff::statements::comparison::comparison_type::LessThan,
					new skiff::statements::value("10")
				), 
				new skiff::statements::self_modifier(
					skiff::statements::self_modifier::modifier_type::PLUS,
					skiff::statements::self_modifier::modifier_time::POST,
					new skiff::statements::variable("x")
				)
			);
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("for(x: Int : lst)");
			p = new skiff::statements::for_itterator_heading(
				new skiff::statements::decleration("x", skiff::statements::type_statement("Int")), 
				new skiff::statements::variable("lst")
			);
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(FlowControls)
		{
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("break");
			p = new skiff::statements::flow_statement(
				skiff::statements::flow_statement::type::BREAK);
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("next");
			p = new skiff::statements::flow_statement(
				skiff::statements::flow_statement::type::NEXT);
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(ImportStatement)
		{
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("import \"localfile\"");
			p = new skiff::statements::import_statement("\"localfile\"");
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("import <extenfile>");
			p = new skiff::statements::import_statement("<extenfile>");
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(DeclarationModifiers)
		{
			skiff::statements::statement * s, *p;

			s = skiff::parse_statement("static def test()");
			p = new skiff::statements::modifier(
				skiff::statements::modifier::modifier_type::STATIC, 
				new skiff::statements::function_heading("test"));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("private def test()");
			p = new skiff::statements::modifier(
				skiff::statements::modifier::modifier_type::PRIVATE, 
				new skiff::statements::function_heading("test"));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("private static def test()");
			p = new skiff::statements::modifier(
				skiff::statements::modifier::modifier_type::PRIVATE,
				new skiff::statements::modifier(
					skiff::statements::modifier::modifier_type::STATIC, 
					new skiff::statements::function_heading("test")));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("static test: String");
			p = new skiff::statements::modifier(skiff::statements::modifier::modifier_type::STATIC,
				new skiff::statements::decleration("test", skiff::statements::type_statement("String")));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("private test: String");
			p = new skiff::statements::modifier(skiff::statements::modifier::modifier_type::PRIVATE,
				new skiff::statements::decleration("test", skiff::statements::type_statement("String")));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("private static test: String");
			p = new skiff::statements::modifier(skiff::statements::modifier::modifier_type::PRIVATE,
				new skiff::statements::modifier(skiff::statements::modifier::modifier_type::STATIC,
					new skiff::statements::decleration("test", skiff::statements::type_statement("String"))
				));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(Annotation)
		{
			skiff::statements::statement * s, *p;
			vector<skiff::statements::statement *> anno_params;
			vector<skiff::statements::function_heading::function_parameter> func_params;

			s = skiff::parse_statement("@Anno def test()");
			p = new skiff::statements::annotation_tag("Anno", anno_params,
				new skiff::statements::function_heading("test", func_params,
					skiff::statements::type_statement("")));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("@Anno(param) def test()");
			anno_params = vector<skiff::statements::statement *>();
			anno_params.push_back(new skiff::statements::variable("param"));
			p = new skiff::statements::annotation_tag("Anno", anno_params,
				new skiff::statements::function_heading("test", func_params,
					skiff::statements::type_statement("")));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("@Anno(param, paramz) def test()");
			anno_params = vector<skiff::statements::statement *>();
			anno_params.push_back(new skiff::statements::variable("param"));
			anno_params.push_back(new skiff::statements::variable("paramz"));
			p = new skiff::statements::annotation_tag("Anno", anno_params,
				new skiff::statements::function_heading("test", func_params,
					skiff::statements::type_statement("")));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("@Anno def test(argz: String): Int");
			func_params = vector<skiff::statements::function_heading::function_parameter>();
			func_params.push_back(
				skiff::statements::function_heading::create_function_parameter("argz",
					skiff::statements::type_statement("String"))
			);
			p = new skiff::statements::annotation_tag("Anno", anno_params,
				new skiff::statements::function_heading("test", func_params,
					skiff::statements::type_statement("Int")));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("@Anno(param) def test(argz: String): Int");
			anno_params = vector<skiff::statements::statement *>();
			anno_params.push_back(new skiff::statements::variable("param"));
			func_params = vector<skiff::statements::function_heading::function_parameter>();
			func_params.push_back(
				skiff::statements::function_heading::create_function_parameter("argz",
					skiff::statements::type_statement("String"))
			);
			p = new skiff::statements::annotation_tag("Anno", anno_params,
				new skiff::statements::function_heading("test", func_params,
					skiff::statements::type_statement("Int")));
			Assert::AreEqual(p->parse_string(), s->parse_string());


			s = skiff::parse_statement("@Anno(param, paramz) def test(argz: String, a: Char): Int");
			anno_params = vector<skiff::statements::statement *>();
			anno_params.push_back(new skiff::statements::variable("param"));
			anno_params.push_back(new skiff::statements::variable("paramz"));
			func_params = vector<skiff::statements::function_heading::function_parameter>();
			func_params.push_back(
				skiff::statements::function_heading::create_function_parameter("argz",
					skiff::statements::type_statement("String"))
			);
			func_params.push_back(
				skiff::statements::function_heading::create_function_parameter("a",
					skiff::statements::type_statement("Char"))
			);
			p = new skiff::statements::annotation_tag("Anno", anno_params,
				new skiff::statements::function_heading("test", func_params,
					skiff::statements::type_statement("Int")));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}

		TEST_METHOD(SwitchMatchHeading)
		{
			skiff::statements::statement * s, *p;
			vector<string> struct_params;

			s = skiff::parse_statement("switch(x)");
			p = new skiff::statements::switch_heading(
				skiff::statements::switch_heading::type::SWITCH,
				new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("match(x)");
			p = new skiff::statements::switch_heading(
				skiff::statements::switch_heading::type::MATCH,
				new skiff::statements::variable("x"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("case \"test\" =>");
			p = new skiff::statements::switch_case_heading(
				new skiff::statements::value("\"test\""));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("case 5 =>");
			p = new skiff::statements::switch_case_heading(new skiff::statements::value("5"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("case val: ClassClass =>");
			p = new skiff::statements::match_case_heading("val",
				skiff::statements::type_statement("ClassClass"));
			Assert::AreEqual(p->parse_string(), s->parse_string());

			struct_params = vector<string>();
			struct_params = { "p1", "p2" };
			s = skiff::parse_statement("case val: StructClass(p1, p2) =>");
			p = new skiff::statements::match_case_heading("val",
				skiff::statements::type_statement("StructClass"), struct_params);
			Assert::AreEqual(p->parse_string(), s->parse_string());

			s = skiff::parse_statement("case _ =>");
			p = new skiff::statements::switch_case_heading(new skiff::statements::variable("_"));
			Assert::AreEqual(p->parse_string(), s->parse_string());
		}
	};
}