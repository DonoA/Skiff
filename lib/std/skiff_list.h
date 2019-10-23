#pragma once

#include <stdbool.h>
#include <string.h>
#include "skiff_anyref.h"

void * skalloc(size_t, size_t);

typedef struct skiff_list_struct skiff_list_t;
void * skiff_list_get_sub_int(skiff_list_t *, int32_t);
void skiff_list_assign_sub_int(skiff_list_t *, void *, int32_t);
int32_t skiff_list_get_size(skiff_list_t *);
struct skiff_list_class_struct 
{
    int32_t class_refs;
    int32_t struct_size;
    void * parent;
    char * simple_name;
    void * (*getSub)(skiff_list_t *, int32_t);
    void (*assignSub)(skiff_list_t *, void *, int32_t);
    int32_t (*getSize)(skiff_list_t *);
};
struct skiff_list_class_struct skiff_list_interface;

struct skiff_list_struct 
{
    struct skiff_list_class_struct * class_ptr;
    uint8_t mark;
    uint32_t size;
    skiff_any_ref_t ** data;
};

void skiff_list_static()
{
    skiff_list_interface.struct_size = sizeof(skiff_list_t);
    skiff_list_interface.class_refs = 0;
    skiff_list_interface.simple_name = "List";
    skiff_list_interface.parent = &skiff_any_ref_interface;
    skiff_list_interface.getSub = skiff_list_get_sub_int;
    skiff_list_interface.assignSub = skiff_list_assign_sub_int;
    skiff_list_interface.getSize = skiff_list_get_size;
}

skiff_list_t * skiff_list_new_0(skiff_list_t * this, int32_t size)
{
    skiff_list_static();
    if(this == 0) {
        this = skalloc(1, sizeof(skiff_list_t) + (size * sizeof(void *)));
        this->class_ptr = &skiff_list_interface;
    }
    this->size = size;
    this->data = (skiff_any_ref_t **) (this + 1);
    return this;
}

void * skiff_list_get_sub_int(skiff_list_t * this, int32_t sub)
{
    return this->data[sub];
}

void skiff_list_assign_sub_int(skiff_list_t * this, void * elt, int32_t sub)
{
    this->data[sub] = elt;
}

int32_t skiff_list_get_size(skiff_list_t * this) 
{
    return this->size;
}