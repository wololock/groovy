/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.ast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Represents a variable scope. This is primarily used to determine variable sharing
 * across method and closure boundaries.
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class VariableScope  {

    private Set declaredVariables = new HashSet();
    private Set referencedVariables = new HashSet();
    private VariableScope parent;
    private List children = new ArrayList();

    public VariableScope() {
    }

    public VariableScope(VariableScope parent) {
        this.parent = parent;
        parent.children.add(this);
    }

    public Set getDeclaredVariables() {
        return declaredVariables;
    }

    public Set getReferencedVariables() {
        return referencedVariables;
    }

    /**
     * @return all the child scopes
     */
    public List getChildren() {
        return children;
    }

    /**
     * Creates a composite variable scope combining all the variable references
     * and declarations from all the child scopes not including this scope
     *
     * @return
     */
    public VariableScope createCompositeChildScope() {
        VariableScope answer = new VariableScope();
        for (Iterator iter = children.iterator(); iter.hasNext(); ) {
            answer.appendRecursive((VariableScope) iter.next());
        }
        answer.parent = this;
        return answer;
    }

    /**
     * Creates a scope including this scope and all nested scopes combined together
     *
     * @return
     */
    public VariableScope createRecursiveChildScope() {
        VariableScope answer = createCompositeChildScope();
        answer.referencedVariables.addAll(referencedVariables);
        answer.declaredVariables.addAll(declaredVariables);
        return answer;
    }

    /**
     * Creates a scope including this scope and all parent scopes combined together
     *
     * @return
     */
    public VariableScope createRecursiveParentScope() {
        VariableScope answer = new VariableScope();
        VariableScope node = this;
        do {
            answer.append(node);
            node = node.parent;
        }
        while (node != null);
        return answer;
    }

    /**
     * Appends all of the references and declarations from the given scope
     * to this one
     *
     * @param scope
     */
    protected void append(VariableScope scope) {
        referencedVariables.addAll(scope.referencedVariables);
        declaredVariables.addAll(scope.declaredVariables);
    }

    /**
     * Appends all of the references and declarations from the given scope
     * and all its children to this one
     *
     * @param scope
     */
    protected void appendRecursive(VariableScope scope) {
        append(scope);

        // now lets traverse the children
        for (Iterator iter = scope.children.iterator(); iter.hasNext(); ) {
            appendRecursive((VariableScope) iter.next());
        }
     }
}
