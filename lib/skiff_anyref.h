#pragma once

#include "allocator.h"

typedef struct skiff_anyref_struct skiff_anyref_t;

struct skiff_anyref_struct 
{
    void * class_ptr;
};