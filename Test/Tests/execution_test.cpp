#include "../../Core/statement.h"
#include "interpreter/builtin.h"
#include "../../Core/utils.h"
#include "interpreter/evaluated_types.h"
#include "../test_util.h"
#include <string>
#include <vector>
#include <queue>
#include <iostream>

using std::string;
using std::vector;
using std::queue;

using skiff::environment::scope;

using namespace skiff::statements;

namespace Test
{
    TEST_CLASS(Execution, 4)
    {
        TEST_METHOD(AssignmentAndDeclaration)
        {
            statement *p;
            scope env = scope();
            skiff::builtin::load::load_standards(&env);

            p = new declaration_with_assignment(
                    "x", type_statement("skiff.lang.Int", vector<type_statement>(), nullptr),
                    new value("5"));
            p->eval(&env);
            Assert::AreEqual(env.get_debug_string(), "x:skiff.lang.Int=5;");
        }
    }
}