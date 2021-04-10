/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.cdi.transaction;

import junit.framework.TestCase;

import jakarta.transaction.*;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * User: paulparkinson Date: 12/10/12 Time: 3:50 PM
 */
public class TransactionalAnnotationTest extends TestCase {

    public static void main(String args[]) throws Exception {
        TransactionalAnnotationTest transactionalAnnotationTest = new TransactionalAnnotationTest();
        transactionalAnnotationTest.testTransactionalInterceptorMANDATORY();
        transactionalAnnotationTest.testTransactionalInterceptorNEVER();
        transactionalAnnotationTest.testTransactionalInterceptorNOT_SUPPORTED();
        transactionalAnnotationTest.testTransactionalInterceptorREQUIRED();
        transactionalAnnotationTest.testTransactionalInterceptorREQUIRES_NEW();
        transactionalAnnotationTest.testTransactionalInterceptorSUPPORTS();
    }

    public void testTransactionalInterceptorMANDATORY() throws Exception {
        TransactionalInterceptorMandatory transactionalInterceptorMANDATORY = new TransactionalInterceptorMandatory();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        transactionalInterceptorMANDATORY.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanMandatory.class.getMethod("foo", String.class), null);
        try {
            transactionalInterceptorMANDATORY.transactional(ctx);
            fail("should have thrown TransactionRequiredException due to " + "transactionalInterceptorMANDATORY and no tx in place");
        } catch (TransactionalException transactionalException) {
            assertTrue("transactionalException.getCause() instanceof TransactionRequiredException",
                    transactionalException.getCause() instanceof TransactionRequiredException);
        }
        transactionManager.begin();
        transactionalInterceptorMANDATORY.transactional(ctx);
        transactionManager.commit();
        try {
            transactionalInterceptorMANDATORY.transactional(ctx);
            fail("should have thrown TransactionRequiredException due to " + "transactionalInterceptorMANDATORY and no tx in place");
        } catch (TransactionalException transactionalException) {
            assertTrue("transactionalException.getCause() instanceof TransactionRequiredException",
                    transactionalException.getCause() instanceof TransactionRequiredException);
        }
    }

    public void testTransactionalInterceptorNEVER() throws Exception {
        TransactionalInterceptorNever transactionalInterceptorNEVER = new TransactionalInterceptorNever();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        transactionalInterceptorNEVER.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanNever.class.getMethod("foo", String.class), null);
        transactionalInterceptorNEVER.transactional(ctx);
        transactionManager.begin();
        try {
            transactionalInterceptorNEVER.transactional(ctx);
            fail("should have thrown InvalidTransactionException due to " + "TransactionalInterceptorNEVER and  tx in place");
        } catch (TransactionalException transactionalException) {
            assertTrue("transactionalException.getCause() instanceof InvalidTransactionException",
                    transactionalException.getCause() instanceof InvalidTransactionException);
        } finally {
            transactionManager.rollback();
        }
    }

    public void testTransactionalInterceptorNOT_SUPPORTED() throws Exception {
        TransactionalInterceptorNotSupported transactionalInterceptorNOT_SUPPORTED = new TransactionalInterceptorNotSupported();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        transactionalInterceptorNOT_SUPPORTED.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanNotSupported.class.getMethod("foo", String.class), null);
        transactionalInterceptorNOT_SUPPORTED.transactional(ctx);
    }

    public void testTransactionalInterceptorREQUIRED() throws Exception {
        TransactionalInterceptorRequired transactionalInterceptorREQUIRED = new TransactionalInterceptorRequired();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        transactionalInterceptorREQUIRED.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanRequired.class.getMethod("foo", String.class), null);
        transactionalInterceptorREQUIRED.transactional(ctx);
        transactionManager.begin();
        transactionalInterceptorREQUIRED.transactional(ctx);
        transactionManager.commit();
        //todo equality check
    }

    public void testTransactionalInterceptorREQUIRES_NEW() throws Exception {
        TransactionalInterceptorRequiresNew transactionalInterceptorREQUIRES_NEW = new TransactionalInterceptorRequiresNew();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        transactionalInterceptorREQUIRES_NEW.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanRequiresNew.class.getMethod("foo", String.class), null);
        transactionalInterceptorREQUIRES_NEW.transactional(ctx);
        transactionManager.begin();
        transactionalInterceptorREQUIRES_NEW.transactional(ctx);
        transactionManager.commit();
        //todo equality check
    }

    public void testTransactionalInterceptorSUPPORTS() throws Exception {
        TransactionalInterceptorSupports transactionalInterceptorSUPPORTS = new TransactionalInterceptorSupports();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        transactionalInterceptorSUPPORTS.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(BeanSupports.class.getMethod("foo", String.class), null);
        transactionalInterceptorSUPPORTS.transactional(ctx);
        transactionManager.begin();
        transactionalInterceptorSUPPORTS.transactional(ctx);
        transactionManager.commit();
    }

    public void testSpecRollbackOnDontRollbackOnSample() throws Exception {

        TransactionalInterceptorRequired transactionalInterceptorREQUIRED = new TransactionalInterceptorRequired();
        jakarta.transaction.TransactionManager transactionManager = new TransactionManager();
        transactionalInterceptorREQUIRED.setTestTransactionManager(transactionManager);
        jakarta.interceptor.InvocationContext ctx = new InvocationContext(
                BeanSpecExampleOfRollbackDontRollback.class.getMethod("throwSQLException"), null) {
            @Override
            public Object getTarget() {
                return new BeanSpecExampleOfRollbackDontRollback();
            }

            @Override
            public Object proceed() throws Exception {
                throw new SQLException("test SQLException");
            }
        };
        transactionManager.begin();
        try {
            transactionalInterceptorREQUIRED.transactional(ctx);
        } catch (SQLException sqlex) {
        }
        try {
            transactionManager.commit();
            fail("should have thrown RollbackException due to mark for rollback");
        } catch (RollbackException rbe) {
        }

        // Now with a child of SQLException
        ctx = new InvocationContext(BeanSpecExampleOfRollbackDontRollback.class.getMethod("throwSQLException"), null) {
            @Override
            public Object getTarget() {
                return new BeanSpecExampleOfRollbackDontRollback();
            }

            @Override
            public Object proceed() throws Exception {
                throw new SQLExceptionExtension();
            }
        };
        transactionManager.begin();
        try {
            transactionalInterceptorREQUIRED.transactional(ctx);
        } catch (SQLExceptionExtension sqlex) {
        }
        try {
            transactionManager.commit();
            fail("should have thrown RollbackException due to mark for rollback");
        } catch (RollbackException rbe) {
        }

        // now with a child of SQLException but one that is specified as dontRollback
        ctx = new InvocationContext(BeanSpecExampleOfRollbackDontRollback.class.getMethod("throwSQLWarning"), null) {
            @Override
            public Object proceed() throws Exception {
                throw new SQLWarning("test SQLWarning");
            }

            @Override
            public Object getTarget() {
                return new BeanSpecExampleOfRollbackDontRollback();
            }

        };
        transactionManager.begin();
        try {
            transactionalInterceptorREQUIRED.transactional(ctx);
        } catch (SQLWarning sqlex) {
        }
        try {
            transactionManager.commit();
        } catch (Exception rbe) {
            fail("should not thrown Exception");
        }

        // now with a child of SQLWarning but one that is specified as rollback
        // ie testing this
        // @Transactional(
        //  rollbackOn = {SQLException.class, SQLWarningExtension.class},
        //  dontRollbackOn = {SQLWarning.class})
        //   where dontRollbackOn=SQLWarning overrides rollbackOn=SQLException,
        //   but rollbackOn=SQLWarningExtension overrides dontRollbackOn=SQLWarning
        // ie...
        //        SQLException isAssignableFrom SQLWarning
        //        SQLWarning isAssignableFrom SQLWarningExtensionExtension
        //        SQLWarningExtensionExtension isAssignableFrom SQLWarningExtension

        ctx = new InvocationContext(BeanSpecExampleOfRollbackDontRollbackExtension.class.getMethod("throwSQLWarning"), null) {
            @Override
            public Object proceed() throws Exception {
                throw new SQLWarningExtension();
            }

            @Override
            public Object getTarget() {
                return new BeanSpecExampleOfRollbackDontRollbackExtension();
            }

        };
        transactionManager.begin();
        try {
            transactionalInterceptorREQUIRED.transactional(ctx);
        } catch (SQLWarningExtension sqlex) {
        }
        try {
            transactionManager.commit();
            fail("should have thrown RollbackException due to mark for rollback");
        } catch (RollbackException rbe) {
        }

        //same as above test but with extension just to show continued inheritance...
        ctx = new InvocationContext(BeanSpecExampleOfRollbackDontRollbackExtension.class.getMethod("throwSQLWarning"), null) {
            @Override
            public Object proceed() throws Exception {
                throw new SQLWarningExtensionExtension();
            }

            @Override
            public Object getTarget() {
                return new BeanSpecExampleOfRollbackDontRollbackExtension();
            }

        };
        transactionManager.begin();
        try {
            transactionalInterceptorREQUIRED.transactional(ctx);
        } catch (SQLWarningExtensionExtension sqlex) {
        }
        try {
            transactionManager.commit();
            fail("should have thrown RollbackException due to mark for rollback");
        } catch (RollbackException rbe) {
        }

    }

    class SQLExceptionExtension extends SQLException {
    }

    class SQLWarningExtension extends SQLWarning {
    }

    class SQLWarningExtensionExtension extends SQLWarningExtension {
    }

    @Transactional(rollbackOn = { SQLException.class, SQLWarningExtension.class }, dontRollbackOn = { SQLWarning.class })
    class BeanSpecExampleOfRollbackDontRollbackExtension extends BeanSpecExampleOfRollbackDontRollback {

    }

}
