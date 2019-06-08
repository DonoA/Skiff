#pragma once

#include <stdint.h>
#include <stdio.h>
#include "skiff_string.h"
#include "skiff_list.h"
#include "skiff_int.h"

void print(skiff_string_t *);
void println(skiff_string_t *);

int main(int, char **);
int32_t * skiff_main(skiff_list_t *);

int main(int argc, char * argv[]) 
{
    skiff_list_t * argz = skiff_list_new();
    for(size_t i = 0; i < argc; i++)
    {
        skiff_list_append(argz, skiff_string_new(argv[0]));
    }
    int32_t * rtn = skiff_main(argz);
    return *rtn;
}

void println(skiff_string_t * string)
{
    print(string);
    putchar('\n');
}

void print(skiff_string_t * string)
{
    for(size_t i = 0; i < string->len; i++) {
        putchar(string->data[i]);
    }
}