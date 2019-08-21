#pragma once

#include <stdbool.h>
#include <string.h>
#include <string.h>

typedef struct {
    size_t id;
    size_t _cap;
    int32_t size;
    void ** data;
} skiff_list_t;

skiff_list_t * skiff_list_new()
{
    skiff_list_t * new_list = (skiff_list_t *) calloc(1, sizeof(skiff_list_t));
    new_list->size = 0;
    new_list->_cap = 10;
    new_list->data = calloc(10, sizeof(void *));
    return new_list;
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