#include <queue>
#include <iostream>
#include <string>
#include "../Core/types.h"
#include "../Core/statement.h"
#include "../Core/parsers.h"
#include "../Core/utils.h"
#include "../Core/builtin.h"
#include "../Core/modes.h"

int main(int argc, char * argv[])
{
	skiff::environment::scope env;

	skiff::builtin::load::load_standards(&env);

	std::cout << "=== Parse Tree ===" << std::endl;
	std::queue<skiff::statements::statement *> test_statements = 
		skiff::modes::parse_file((argc == 2 ? std::string(argv[1]) : "test.su"), true);
	std::cout << "=== Evaluation ===" << std::endl;
	skiff::modes::evaluate(&env, test_statements);
	std::cout << "=== Ending Scope ===" << std::endl;
	std::cout << env.get_debug_string() << std::endl;
#if (defined (_WIN32) || defined (_WIN64))
	system("pause");
#endif
}

