#pragma once

#include <stdbool.h>
#include <string.h>

typedef struct skiff_list_struct skiff_list_t;
void * skiff_list_get_sub_int(skiff_list_t *, int32_t);
void skiff_list_append(skiff_list_t *, void *);
int32_t skiff_list_get_size(skiff_list_t *);
struct skiff_list_class_struct 
{
    void * (*getSub)(skiff_list_t *, int32_t);
    void (*append)(skiff_list_t *, void *);
    int32_t (*getSize)(skiff_list_t *);
};
struct skiff_list_class_struct skiff_list_interface;
void skiff_list_static()
{
    skiff_list_interface.getSub = skiff_list_get_sub_int;
    skiff_list_interface.append = skiff_list_append;
    skiff_list_interface.getSize = skiff_list_get_size;
}
struct skiff_list_struct 
{
    struct skiff_list_class_struct * class_ptr;
    uint32_t _cap;
    uint32_t size;
    void ** data;
};

skiff_list_t * skiff_list_new(skiff_list_t * this, int new_inst)
{
    skiff_list_static();
    if(new_inst) {
        this->class_ptr = &skiff_list_interface;
    }
    this->size = 0;
    this->_cap = 10;
    this->data = skalloc(10, sizeof(void *));
    return this;
}

skiff_list_t * skiff_list_allocate_new()
{
    return skiff_list_new((skiff_list_t *) skalloc(1, sizeof(skiff_list_t)), 1);
}

void * skiff_list_get_sub_int(skiff_list_t * this, int32_t sub)
{
    return this->data[sub];
}

void skiff_list_append(skiff_list_t * this, void * dat)
{
    if(this->size >= this->_cap)
    {
        void * old_data = this->data;
        this->data = calloc(this->_cap*2, sizeof(void *));
        this->_cap *= 2;
        memcpy(this->data, old_data, sizeof(void *) * this->size);
        free(this->data);
    }

    this->data[this->size] = dat;
    this->size++;
}

int32_t skiff_list_get_size(skiff_list_t * this) 
{
    return this->size;
}