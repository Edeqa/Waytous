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
                var name = x;
                if(name == "className") name = "class";
                el.setAttribute(name, properties[x]);
            }
        }
        if(appendTo) {
            appendTo.appendChild(el);
        }

        return el;
    }

    var clear = function(node) {
        if(!node) return;
        for(var i = node.children.length-1; i>=0; i--) {
            node.removeChild(node.children[i]);
        }
    }

    return {
        create: create,
        clear: clear,
    }
}
