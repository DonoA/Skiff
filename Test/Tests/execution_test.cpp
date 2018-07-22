#include "../../Core/statement.h"
#include "../../Core/builtin.h"
#include "../../Core/utils.h"
#include "../../Core/types.h"
#include "../test_util.h"
#include <string>
#include <vector>
#include <queue>
#include <iostream>

using std::string;
using std::vector;
using std::queue;

using skiff::environment::scope;

namespace Test
{
	TEST_CLASS(Execution, 4)
	{
		TEST_METHOD(AssignmentAndDeclaration)
		{
			skiff::statements::statement * p;
			scope env = scope();
			skiff::builtin::load::load_standards(&env);
			
			p = new skiff::statements::declaration_with_assignment(
				"x", skiff::statements::type_statement("Int"), 
				new skiff::statements::value("5"));
			p->eval(&env);
			Assert::AreEqual(env.get_debug_string(), "x:Int=5;");
		}
	}
}