#include "modes.h"

namespace skiff
{
    namespace modes
    {
        using ::std::string;
        using ::std::vector;
        using ::std::queue;
        using ::std::stack;
        using ::std::ofstream;
        using statements::statement;
        using statements::braced_block;
        using environment::scope;

        vector<statement *> parse_file(string infile)
        {
            std::fstream fin(infile, std::fstream::in);
            string file;
            string line;
            while (std::getline(fin, line))
            {
                file += line + "\n";
            }
            vector<tokenizer::token> token_sequence = tokenizer::tokenize(file);

            new_parser parser(token_sequence);

            vector<statement *> statements = parser.parse_statement();

            return statements;
        }

        void evaluate(scope *env, vector<statement *> statements)
        {
            for (statement *s : statements)
            {
                s->eval(env);
            }
        }

        void compile(compilation_types::compilation_scope *env, vector<statement *> statements, string outfile)
        {
            ofstream output;
            output.open(outfile);
            vector<string> main_body = {"\tscratch = malloc(1024 *4); // Malloc scratch region"};
            for (statement *s : statements)
            {
                for(string ln : s->compile(env).content)
                {
                    main_body.push_back("\t" + ln + ";");
                }
            }
            env->declare_function(
                    "main",
                    "main",
                    "int main (int argc, char **argv)",
                    main_body, statements::type_statement("Int"));
            env->unroll(&output);
            output.close();
        }
    }
}
