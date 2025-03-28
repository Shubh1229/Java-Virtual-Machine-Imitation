package ast.visitors;

import java.util.ArrayList;
import java.util.HashMap;

import ast.model.AssignStatement;
import ast.model.BinaryExpression;
import ast.model.CompoundStatement;
import ast.model.ConstantExpression;
import ast.model.Expression;
import ast.model.IfStatement;
import ast.model.Statement;
import ast.model.VariableExpression;
import ast.model.WhileStatement;
import engine.opcodes.Operator;

public class OptimizingVisitor extends CloneVisitor {
    private ArrayList<HashMap<String,Integer>> priorityValues = new ArrayList<>();
    private int index = 0;
    public OptimizingVisitor(){
        priorityValues.add(new HashMap<String,Integer>());
    }
    @Override
    public void visit(VariableExpression expr) {
        if(expr.isLValue()){
            statements.add(new AssignStatement(expr, exprStack.pop()));
        } else {
            exprStack.push(expr);
        }
    }

    @Override
    public void postVisit(BinaryExpression expr) {
        if(expr.getLeftChild().isLValue())return;
        Operator op = expr.getOperator();
        HashMap<String,Integer> variableValues = priorityValues.get(index);
        if(exprStack.peek() instanceof ConstantExpression){
            ConstantExpression right = (ConstantExpression) exprStack.pop();
            if(exprStack.peek() instanceof ConstantExpression){
                ConstantExpression left = (ConstantExpression) exprStack.pop();
                int total = op.apply(left.getValue(), right.getValue());
                exprStack.push(new ConstantExpression(total));
            }else {
                VariableExpression left = (VariableExpression) exprStack.pop();
                if(variableValues.containsKey(left.getName()) && variableValues.get(left.getName()) != null){
                    int total = op.apply(variableValues.get(left.getName()), right.getValue());
                    exprStack.push(new ConstantExpression(total));
                } else {
                    exprStack.push(new BinaryExpression(left, op, right));
                }
            }    
        } else if(exprStack.peek() instanceof VariableExpression){
            VariableExpression right = (VariableExpression) exprStack.pop();
            if(variableValues.containsKey(right.getName()) && variableValues.get(right.getName()) != null){
                if(exprStack.peek() instanceof ConstantExpression){
                    ConstantExpression left = (ConstantExpression) exprStack.pop();
                    int total = op.apply(left.getValue(), variableValues.get(right.getName()));
                    exprStack.push(new ConstantExpression(total));
                }else {
                    VariableExpression left = (VariableExpression) exprStack.pop();
                    if(variableValues.containsKey(left.getName()) && variableValues.get(left.getName()) != null){
                        int total = op.apply(variableValues.get(left.getName()), variableValues.get(right.getName()));
                        exprStack.push(new ConstantExpression(total));
                    } else {
                        exprStack.push(new BinaryExpression(left, op, right));
                    }
                }    
            } else {
                exprStack.push(new BinaryExpression(exprStack.pop(), op, right));
            }
        } else if(exprStack.peek() instanceof BinaryExpression){
            Expression right = exprStack.pop();
            Expression left = exprStack.pop();
            exprStack.push(new BinaryExpression(left, op, right));
        } else {
            Expression right = exprStack.pop();
            Expression left = exprStack.pop();
            exprStack.push(new BinaryExpression(left, op, right));
        }
    }

    @Override
    public void postVisit(AssignStatement stmt) {
        AssignStatement currentStmt = (AssignStatement) statements.get(statements.size()-1);
        VariableExpression left = (VariableExpression) currentStmt.getLValue();
        HashMap<String,Integer> variableValues = priorityValues.get(index);
        if(currentStmt.getValue() instanceof ConstantExpression){
            ConstantExpression value = (ConstantExpression) currentStmt.getValue();
            variableValues.put(left.getName(), value.getValue());
            priorityValues.get(index).put(left.getName(), value.getValue());
        } else {
            variableValues.put(left.getName(), null);
        }
        
    }
    @Override
    public void preVisit(IfStatement stmt){
        index++;
        priorityValues.add(new HashMap<String,Integer>());
        for(HashMap<String,Integer> map : priorityValues){
            priorityValues.get(index).putAll(map);
        }
    }
    @Override
    public void preElseVisit(IfStatement stmt){
        index++;
        priorityValues.add(new HashMap<String,Integer>());
        for(int i = 0; i < index-1; i++){
            priorityValues.get(index).putAll(priorityValues.get(i));
        }
    }

    @Override
    public void postVisit(IfStatement stmt) {
        Expression condition = exprStack.pop();
        int result = -1;
        HashMap<String,Integer> variableValues = priorityValues.get(index-1);
        if(stmt.getElseBlock() != null ) variableValues = priorityValues.get(index-2);
        if(condition instanceof BinaryExpression){
            BinaryExpression con = (BinaryExpression) condition;
            Operator op = con.getOperator();
            Expression left = con.getLeftChild();
            Expression right = con.getRightChild();
            if(left instanceof ConstantExpression && right instanceof ConstantExpression){
                ConstantExpression rightconst = (ConstantExpression) right;
                ConstantExpression leftconst = (ConstantExpression) left;
                result = op.apply(leftconst.getValue(), rightconst.getValue());
            } else if(left instanceof VariableExpression || right instanceof VariableExpression){
                if(left instanceof VariableExpression && right instanceof VariableExpression ){
                    VariableExpression leftV = (VariableExpression) left;
                    VariableExpression rightV = (VariableExpression) right;
                    if((variableValues.containsKey(leftV.getName()) && variableValues.get(leftV.getName()) != null) && (variableValues.containsKey(rightV.getName()) && variableValues.get(rightV.getName()) != null)){
                        result = op.apply(variableValues.get(leftV.getName()), variableValues.get(rightV.getName()));
                    }
                } else if(left instanceof VariableExpression && right instanceof ConstantExpression){
                    ConstantExpression rightC = (ConstantExpression) right;
                    VariableExpression leftV = (VariableExpression) left;
                    if(variableValues.containsKey(leftV.getName()) && variableValues.get(leftV.getName()) != null){
                        result = op.apply(variableValues.get(leftV.getName()), rightC.getValue());
                    }
                } else if(right instanceof VariableExpression && left instanceof ConstantExpression){
                    ConstantExpression leftC = (ConstantExpression) left;
                    VariableExpression rightV = (VariableExpression) right;
                    if(variableValues.containsKey(rightV.getName()) && variableValues.get(rightV.getName()) != null){
                        result = op.apply(variableValues.get(rightV.getName()), leftC.getValue());
                    }
                }
            }

        } else if (condition instanceof ConstantExpression){
            ConstantExpression conCon = (ConstantExpression) condition;
            result = conCon.getValue();
        }
        //in this block we need to check for an AssignStatement to update the variableValues
        if(stmt.getElseBlock() != null){
            // add Statement if from statements and the else statement to run the forloop
            CompoundStatement elseStatement = (CompoundStatement) statements.remove(statements.size()-1);
            CompoundStatement ifStatement = (CompoundStatement) statements.remove(statements.size()-1);
            if(result == 1){
                priorityValues.remove(index);
                index--;
                variableValues = priorityValues.get(index);
                for(Statement s : ifStatement.getBody()){
                    statements.add(s);
                }
                //for (Statement s : ifstatement)
            } else if (result == 0){
                for(Statement s : elseStatement.getBody()){
                    statements.add(s);
                }
                priorityValues.remove(index);
                index--;
                //for (Statement s : elsestatement)
            } else {
                statements.add(new IfStatement(condition, ifStatement, elseStatement));
                priorityValues.remove(index);
                index--;
            }
        } else {
            //need to add something for result == 1 to add the actual statements
            CompoundStatement ifStatement = (CompoundStatement) statements.remove(statements.size()-1);
            if(result == 1){
                for(Statement s : ifStatement.getBody()){
                    statements.add(s);
                    if(s instanceof AssignStatement){
                        AssignStatement value = (AssignStatement) s;
                        VariableExpression lValue = (VariableExpression) value.getLValue();
                        ConstantExpression vValue = (ConstantExpression) value.getValue();
                        variableValues.put(lValue.getName(), vValue.getValue());
                    }
                }
            }else if(result == 0){
                System.out.println("no need to do anything since if statement is already removed");
                for(Statement s : ifStatement.getBody()){
                    if(s instanceof AssignStatement){
                        AssignStatement value = (AssignStatement) s;
                        VariableExpression lValue = (VariableExpression) value.getLValue();
                        variableValues.remove(lValue.getName());
                    }
                }
            } else {
                statements.add(new IfStatement(condition, ifStatement));
            }
        }
        priorityValues.remove(index);
        index--;
    }

    @Override
    public void preVisit(WhileStatement stmt) {
        //idk what to do here
        System.out.println("preVisit WhileStatment here... did nothing lol");
        index++;
        priorityValues.add(new HashMap<String,Integer>());
        for(HashMap<String,Integer> map : priorityValues){
            priorityValues.get(index).putAll(map);
        }
    }

    @Override
    public void postVisit(WhileStatement stmt) {
        Expression condition = exprStack.pop();
        int result = -1;
        HashMap<String,Integer> variableValues = priorityValues.get(index -1);
        if(condition instanceof BinaryExpression){
            BinaryExpression con = (BinaryExpression) condition;
            Operator op = con.getOperator();
            Expression left = con.getLeftChild();
            Expression right = con.getRightChild();
            if(left instanceof ConstantExpression && right instanceof ConstantExpression){
                ConstantExpression rightconst = (ConstantExpression) right;
                ConstantExpression leftconst = (ConstantExpression) left;
                result = op.apply(leftconst.getValue(), rightconst.getValue());
            } else if(left instanceof VariableExpression || right instanceof VariableExpression){
                if(left instanceof VariableExpression && right instanceof VariableExpression ){
                    VariableExpression leftV = (VariableExpression) left;
                    VariableExpression rightV = (VariableExpression) right;
                    if((variableValues.containsKey(leftV.getName()) && variableValues.get(leftV.getName()) != null) && (variableValues.containsKey(rightV.getName()) && variableValues.get(rightV.getName()) != null)){
                        result = op.apply(variableValues.get(leftV.getName()), variableValues.get(rightV.getName()));
                    }
                } else if(left instanceof VariableExpression && right instanceof ConstantExpression){
                    ConstantExpression rightC = (ConstantExpression) right;
                    VariableExpression leftV = (VariableExpression) left;
                    if(variableValues.containsKey(leftV.getName()) && variableValues.get(leftV.getName()) != null){
                        result = op.apply(variableValues.get(leftV.getName()), rightC.getValue());
                    }
                } else if(right instanceof VariableExpression && left instanceof ConstantExpression){
                    ConstantExpression leftC = (ConstantExpression) left;
                    VariableExpression rightV = (VariableExpression) right;
                    if(variableValues.containsKey(rightV.getName()) && variableValues.get(rightV.getName()) != null){
                        result = op.apply(variableValues.get(rightV.getName()), leftC.getValue());
                    }
                }
            }

        } else if (condition instanceof ConstantExpression){
            ConstantExpression conCon = (ConstantExpression) condition;
            result = conCon.getValue();
            if(result == 1){
                CompoundStatement bstmt = (CompoundStatement) statements.remove(statements.size()-1);
                CompoundStatement bodyogStatement = (CompoundStatement) stmt.getBody();
                int indexx = -1;
                for(Statement s : bodyogStatement.getBody()){
                    AssignStatement values = (AssignStatement) s;
                    if(values.getValue() instanceof BinaryExpression){
                        indexx = bodyogStatement.getBody().indexOf(s);
                    }
                }
                if(indexx != -1){
                    bstmt.getBody().remove(indexx);
                    bstmt.getBody().add(indexx, bodyogStatement.getBody().get(indexx));
                }
                statements.add(new WhileStatement(stmt.getCondition(), bstmt));
                index--;
                return;
            }
        }
        Statement bstmt = statements.remove(statements.size()-1);
        if(result == 1){
            statements.add(new WhileStatement(condition, bstmt));

            
        } else if (result == 0 ){
            // statements.remove(statements.size()-1);
            for(String s : priorityValues.get(index).keySet()){
                if(priorityValues.get(index-1).containsKey(s)){
                    int val = priorityValues.get(index).get(s);
                    priorityValues.get(index-1).put(s,val);
                }
            }
            priorityValues.remove(index);
            
        } else if (result == -1){
            System.out.println("No way to optimize if statement therefore whole if statement is added");
            statements.add(new WhileStatement(condition, bstmt));
        }
        index--;
    }

}