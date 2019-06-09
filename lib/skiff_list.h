#pragma once

#include <stdbool.h>
#include <string.h>
#include <string.h>

typedef struct {
    size_t id;
    size_t _size;
    size_t used;
    void ** data;
} skiff_list_t;

skiff_list_t ** skiff_list_new()
{
    skiff_list_t ** new_list = (skiff_list_t **) skalloc_heap(1, sizeof(skiff_list_t));
    (*new_list)->used = 0;
    (*new_list)->_size = 10;
    (*new_list)->data = skalloc_heap(10, sizeof(void *));
    return new_list;
}

void skiff_list_append(skiff_list_t ** this, void * dat)
{
    if((*this)->used >= (*this)->_size) 
    {
        void * old_data = (*this)->data;
        (*this)->data = skalloc_heap((*this)->_size*2, sizeof(void *));
        (*this)->_size *= 2;
        memcpy((*this)->data, old_data, sizeof(void *) * (*this)->used);
    }

    (*this)->data[(*this)->used] = dat;
    (*this)->used++;
}