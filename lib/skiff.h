#pragma once

#include <stdint.h>
#include <stdio.h>
#include "skiff_string.h"
#include "skiff_list.h"

void print(skiff_string_t *);
void println(skiff_string_t *);

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