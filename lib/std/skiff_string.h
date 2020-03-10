#pragma once

#include <string.h>
#include <stdlib.h>

#include "skiff_anyref.h"
#include "skiff_list.h"

void * skalloc(size_t, size_t);

typedef struct skiff_string_struct skiff_string_t;
uint8_t skiff_string_equals(skiff_string_t *, skiff_string_t *);
struct skiff_string_class_struct 
{
    int32_t class_refs;
    int32_t struct_size;
    void * parent;
    char * simple_name;
    uint8_t (*equals)(skiff_string_t *, skiff_string_t *);
};
struct skiff_string_class_struct skiff_string_interface;
struct skiff_string_struct 
{
    struct skiff_string_class_struct * class_ptr;
    uint8_t mark;
    uint32_t len;
    char * data;
};

void skiff_string_static()
{
    skiff_string_interface.struct_size = sizeof(skiff_string_t);
    skiff_string_interface.class_refs = 0;
    skiff_string_interface.simple_name = "String";
    skiff_string_interface.parent = &skiff_any_ref_interface;
    skiff_string_interface.equals = skiff_string_equals;
}

skiff_string_t * skiff_string_new_0(skiff_string_t * this, char * cstr)
{
    skiff_string_static();
    size_t len = strlen(cstr);
    if(this == 0) {
        this = skalloc(1, sizeof(skiff_string_t) + sizeof(char) * (len + 1));
        this->class_ptr = &skiff_string_interface;
    }
    this->len = strlen(cstr);
    this->data = (char *) (this + 1);
    strcpy(this->data, cstr);
    return this;
}

uint8_t skiff_string_equals(skiff_string_t * s1, skiff_string_t * s2) 
{
    return strcmp(s1->data, s2->data) == 0;
}

