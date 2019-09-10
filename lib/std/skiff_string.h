#pragma once

#include <stdbool.h>
#include <string.h>
#include <stdlib.h>

#include "allocator.h"

typedef struct skiff_string_struct skiff_string_t;
bool skiff_string_equals(skiff_string_t *, skiff_string_t *);
struct skiff_string_class_struct 
{
    bool (*equals)(skiff_string_t *, skiff_string_t *);
};
struct skiff_string_class_struct skiff_string_interface;
void skiff_string_static()
{
    skiff_string_interface.equals = skiff_string_equals;
}
struct skiff_string_struct 
{
    struct skiff_string_class_struct * class_ptr;
    uint32_t len;
    char * data;
};

skiff_string_t * skiff_string_new(skiff_string_t * this, int new_inst, char * cstr)
{
    skiff_string_static();
    if(new_inst) { 
        this->class_ptr = &skiff_string_interface;
    }
    this->len = strlen(cstr);
    this->data = (char *) skalloc(this->len, sizeof(char));
    strcpy(this->data, cstr);
    return this;
}

skiff_string_t * skiff_string_allocate_new(char * cstr)
{
    return skiff_string_new((skiff_string_t *) skalloc(1, sizeof(skiff_string_t)), 1, cstr);
}

bool skiff_string_equals(skiff_string_t * s1, skiff_string_t * s2) 
{
    return strcmp(s1->data, s2->data) == 0;
}