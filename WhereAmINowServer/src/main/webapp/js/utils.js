/**
 * Created 1/20/17.
 */

/**
 * Created 1/19/17.
 */
function Utils() {

    var create = function(name, properties, appendTo) {
        var el = document.createElement(name);

        for(var x in properties) {
            if(x == "innerHTML") {
                el.innerHTML = properties[x];
            } else {
                el.setAttribute(x, properties[x]);
            }
        }
        if(appendTo) {
            appendTo.appendChild(el);
        }

        return el;
    }

    return {
        create: create,

    }
}
