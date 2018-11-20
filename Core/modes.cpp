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

            env->declare_function(
                    "new",
                    "new",
                    "size_t new(size_t len)",
                    {
                            "\tsize_t loc = heap_offset;",
                            "\theap_offset += len;",
                            "\tmemset(heap + loc, 0, len);",
                            "\treturn (size_t) (heap + loc);"
                    },
                    statements::type_statement("Int"));

            vector<string> main_body = {
                    "\theap = malloc(1024 *4); // Malloc heap region",
                    "\tref_heap = malloc(1024 *4); // Malloc name references region"
            };
            for (statement *s : statements)
            {
                auto compiled = s->compile(env);
                for(string ln : compiled.content)
                {
                    ln = "\t" + ln;
                    if(compiled.content.size() == 1)
                    {
                        ln += ";";
                    }
                    main_body.push_back(ln);
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
