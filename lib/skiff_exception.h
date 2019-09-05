#pragma once

#include <stdbool.h>
#include <stdlib.h>

#include "allocator.h"
#include "skiff_string.h"
#include "skiff_anyref.h"

typedef struct skiff_exception_struct skiff_exception_t;
struct skiff_exception_class_struct 
{ 
    void * parent;
    skiff_string_t * (*getMessage)(skiff_exception_t *);
};
struct skiff_exception_class_struct skiff_exception_interface;
skiff_string_t * skiff_exception_get_message(skiff_exception_t *);
typedef struct skiff_exception_class_struct skiff_exception_class_t; 
void skiff_exception_static()
{
    skiff_exception_interface.parent = &skiff_any_ref_interface;
    skiff_exception_interface.getMessage = skiff_exception_get_message;
}
struct skiff_exception_struct 
{
    struct skiff_exception_class_struct * class_ptr;
    skiff_string_t * message;
};

skiff_exception_t * skiff_exception_new(skiff_exception_t * this, int new_inst, skiff_string_t * message)
{
    skiff_exception_static();
    if(new_inst) { 
        this->class_ptr = &skiff_exception_interface;
    }
    this->message = message;
    return this;
}

skiff_exception_t * skiff_exception_allocate_new(skiff_string_t * message)
{
    return skiff_exception_new((skiff_exception_t *) skalloc(1, sizeof(skiff_exception_t)), 1, message);
}

skiff_string_t * skiff_exception_get_message(skiff_exception_t * this) 
{
    return this->message;
}