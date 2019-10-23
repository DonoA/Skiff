#pragma once

#include <stdbool.h>
#include <stdlib.h>

#include "allocator.h"
#include "skiff_string.h"
#include "skiff_anyref.h"

typedef struct skiff_exception_struct skiff_exception_t;
struct skiff_exception_class_struct 
{ 
    int32_t class_refs;
    int32_t struct_size;
    void * parent;
    char * simple_name;
    skiff_string_t * (*getMessage)(skiff_exception_t *);
};
struct skiff_exception_class_struct skiff_exception_interface;
skiff_string_t * skiff_exception_get_message(skiff_exception_t *);
typedef struct skiff_exception_class_struct skiff_exception_class_t; 

struct skiff_exception_struct 
{
    struct skiff_exception_class_struct * class_ptr;
    uint8_t mark;
    skiff_string_t * message;
};

void skiff_exception_static()
{
    skiff_exception_interface.struct_size = sizeof(skiff_exception_t);
    skiff_exception_interface.class_refs = 1;
    skiff_exception_interface.simple_name = "Exception";
    skiff_exception_interface.parent = &skiff_any_ref_interface;
    skiff_exception_interface.getMessage = skiff_exception_get_message;
}

skiff_exception_t * skiff_exception_new_0(skiff_exception_t * this, skiff_string_t * message)
{
    skiff_exception_static();
    if(this == 0) {
        this = skalloc(1, sizeof(skiff_exception_t));
        this->class_ptr = &skiff_exception_interface;
    }
    this->message = message;
    return this;
}

skiff_string_t * skiff_exception_get_message(skiff_exception_t * this) 
{
    return this->message;
}