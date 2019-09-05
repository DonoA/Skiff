#pragma once

#include "allocator.h"

typedef struct skiff_any_ref_struct skiff_any_ref_t;
struct skiff_any_ref_class_struct 
{ 
    void * parent;
};
struct skiff_any_ref_class_struct skiff_any_ref_interface;
struct skiff_any_ref_struct 
{
    void * class_ptr;
};