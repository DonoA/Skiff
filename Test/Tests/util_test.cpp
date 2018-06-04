#include "../../Core/utils.h"
#include "../test_util.h"
#include <string>
#include <vector>
#include <iostream>

using std::string;
using std::vector;

namespace Test
{
	TEST_CLASS(Utils, 1)
	{
		TEST_METHOD(Padding)
		{
			Assert::AreEqual(string("Hello    World"), 
				skiff::utils::remove_pad("  Hello    World   "));
			Assert::AreEqual(string(""), skiff::utils::remove_pad("  "));
		}

		TEST_METHOD(BracedSplit)
		{
			vector<string> exp;
			vector<string> real;
			real = skiff::utils::braced_split("Hello,World", ',');
			exp = vector<string>();
			exp.push_back("Hello");
			exp.push_back("World");
			Assert::IsTrue(real == exp);
			real = skiff::utils::braced_split("\"Hello,World\"", ',');
			exp = vector<string>();
			exp.push_back("\"Hello,World\"");
			Assert::IsTrue(real == exp);
			real = skiff::utils::braced_split("({Hello,World})", ',');
			exp = vector<string>();
			exp.push_back("({Hello,World})");
			Assert::IsTrue(real == exp);
		}

	};
}