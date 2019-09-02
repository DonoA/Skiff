#include <setjmp.h>
#include <stdlib.h>
#include "skiff_exception.h"
#include <stdint.h>

typedef struct skiff_catch_layer_struct skiff_catch_layer_t;

struct skiff_catch_layer_struct {
    void * catch_class_ptr;
    jmp_buf current_catch_state;
    size_t sp_ref_val;
    void (*current_catch)(skiff_catch_layer_t *, skiff_exception_t *);
    skiff_catch_layer_t * next;
    skiff_catch_layer_t * prev;
};

skiff_catch_layer_t * catch_layer_head;
skiff_catch_layer_t * catch_layer_tail;

void skiff_throw(skiff_exception_t * ex) 
{
    // search back in some catch stack to find a catch for this
    skiff_catch_layer_t * layer = catch_layer_tail;

    while(1)
    {
        if(layer->catch_class_ptr == ex->class_ptr)
        {
            break;
        }

        if(layer->prev == NULL) 
        {
            break;
        }

        layer = layer->prev;
    }

    layer->current_catch(layer, ex);
}



void skiff_start_try(void (*ctch)(skiff_catch_layer_t * layer, skiff_exception_t * ex), void * interface, size_t sp_ref_val) 
{
    skiff_catch_layer_t * new_layer = (skiff_catch_layer_t *) calloc(1, sizeof(skiff_catch_layer_t));
    new_layer->catch_class_ptr = interface;
    new_layer->current_catch = ctch;
    new_layer->prev = catch_layer_tail;
    new_layer->sp_ref_val = sp_ref_val;
    catch_layer_tail->next = new_layer;
    catch_layer_tail = new_layer;
}

void skiff_end_try() 
{
    skiff_catch_layer_t * old_layer = catch_layer_tail;

    catch_layer_tail = catch_layer_tail->prev;
    catch_layer_tail->next = NULL;
    free(old_layer);
}