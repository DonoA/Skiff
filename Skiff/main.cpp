#if (defined (_WIN32) || defined (_WIN64))
	#include "stdafx.h"
#endif
#include <queue>
#include <iostream>
#include "../Core/types.h"
#include "../Core/statement.h"
#include "../Core/parsers.h"
#include "../Core/utils.h"
#include "../Core/builtin.h"
#include "../Core/modes.h"

int main()
{
	skiff::environment::scope env;

	skiff::builtin::load::load_standards(&env);

	std::queue<skiff::statements::statement *> test_statements = 
		skiff::modes::parse_file("test.su");
	// skiff::modes::print_parse(test_statements);
	std::cout << "=== Parse Complete ===" << std::endl;
	skiff::modes::evaluate(&env, test_statements);
	std::cout << "=== Eval Complete ===" << std::endl;
	env.print_debug();
#if (defined (_WIN32) || defined (_WIN64))
	system("pause");
#endif
}

