#include "compilation_types.h"

namespace skiff
{
    namespace compilation_types
    {
        void compilation_scope::add_include(string name, bool local)
        {
            this->includes.push_back({name, local});
        }

        void compilation_scope::declare_function(string proto)
        {
            this->defined_functions.push_back({proto, ""});
        }

        void compilation_scope::add_to_top_function(string content)
        {
            this->defined_functions.at(this->defined_functions.size()-1).content += content;
        }

        void compilation_scope::add_to_main_function(string content)
        {
            if(this->defined_functions.size() == 0)
            {
                c_function func;
                func.proto = "int main(int argc, char **argv)";
                func.content = "";
                this->defined_functions.push_back(func);
            }
            this->defined_functions.at(0).content += content;
        }

        void compilation_scope::unroll()
        {
            for(c_function c : this->defined_functions)
            {
                std::cout << c.proto << "\n{\n" << c.content << "}\n";
            }
        }
    }
}
