#include <queue>
#include <iostream>
#include "interpreter/evaluated_types.h"
#include "interpreter/builtin.h"
#include "../Core/modes.h"
#include "compiler/compilation_types.h"

int main(int argc, char *argv[])
{

    bool compile = true;

    std::cout << "=== Parse Tree ===" << std::endl;
    std::vector<skiff::statements::statement *> test_statements =
            skiff::modes::parse_file((argc == 2 ? std::string(argv[1]) : "test.su"));

    for (statement *st : test_statements)
    {
        std::cout << st->parse_string() << std::endl;
    }

    if(compile)
    {
        skiff::compilation_types::compilation_scope *env;

        std::cout << "=== Compilation ===" << std::endl;

        skiff::modes::compile(env, test_statements, "test.c");

    }
    else
    {
        skiff::environment::scope env;

        std::cout << "=== Evaluation ===" << std::endl;
        skiff::builtin::load::load_standards(&env);

        skiff::modes::evaluate(&env, test_statements);
        std::cout << "=== Ending Scope ===" << std::endl;
        skiff::environment::skiff_object a = env.get_variable("a");
        std::cout << env.get_debug_string() << std::endl;
    }
#if (defined (_WIN32) || defined (_WIN64))
    system("pause");
#endif
}

