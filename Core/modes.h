#pragma once
#include "statement.h"

void interactive_mode();
queue<statement *> parse_file(string infile);
void print_parse(queue<statement *> statements);
void translate_c(queue<statement *> statements);
void evaluate(scope * env, queue<statement *> statements);

