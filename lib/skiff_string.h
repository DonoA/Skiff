#pragma once

#include <stdbool.h>
#include <string.h>
#include <stdlib.h>

#include "allocator.h"

typedef struct {
    size_t id;
    size_t len;
    char * data;
} skiff_string_t;

skiff_string_t * skiff_string_new(char * cstr)
{
    skiff_string_t * str = (skiff_string_t *) skalloc(1, sizeof(skiff_string_t));
    str->len = strlen(cstr);
    str->data = (char *) skalloc(str->len, sizeof(char));
    strcpy(str->data, cstr);

    return str;
}

bool skiff_string_equals(skiff_string_t * s1, skiff_string_t * s2) 
{
    return strcmp(s1->data, s2->data) == 0;
}