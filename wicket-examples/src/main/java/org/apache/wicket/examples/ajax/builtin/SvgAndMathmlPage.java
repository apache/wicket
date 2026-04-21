/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.examples.ajax.builtin;

import static org.apache.wicket.core.request.handler.XmlReplacementEnablingBehavior.MATHML_NAMESPACE_URI;
import static org.apache.wicket.core.request.handler.XmlReplacementEnablingBehavior.SVG_NAMESPACE_URI;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.handler.XmlReplacementEnablingBehavior;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * Demo page for XML, showing update of and interaction with SVG and MathML.
 */
public class SvgAndMathmlPage extends BasePage
{
    private static final List<Integer> ONE_TO_NINE = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);

    @Override
    protected void onInitialize()
    {
        super.onInitialize();

        addSvgForm();
        addMathmlForm();
    }

    private void addSvgForm()
    {
        var ticTacToe = new WebMarkupContainer("ticTacToe");
        ticTacToe.add(new XmlReplacementEnablingBehavior(SVG_NAMESPACE_URI));
        add(ticTacToe);

        var boardStateModel = Model.of(newInitialTicTacToeState());
        var nextTurnModel = Model.of(SquareState.CROSS);

        var svgButtonsForm = new Form<>("svgForm");
        svgButtonsForm.setOutputMarkupId(true);
        add(svgButtonsForm);

        var squareLinks = new AjaxLink[9];
        var buttons = new AjaxButton[9];
        for (var column = 'A'; column < 'D'; column += 1)
        {
            for (var row = 1; row < 4; row += 1)
            {
                var squareId = String.valueOf(column) + row;
                var index = (column - 'A') * 3 + row - 1;
                var squareStateModel = LambdaModel.of(boardStateModel,
                        boardState -> boardState[index],
                        (boardState, squareState) -> boardState[index] = squareState);
                var squareLink = new AjaxLink<>("square" + squareId, squareStateModel)
                {
                    @Override
                    protected void onConfigure()
                    {
                        super.onConfigure();

                        setEnabled(getModelObject() == SquareState.EMPTY);
                    }

                    @Override
                    public void onClick(AjaxRequestTarget target)
                    {
                        setModelObject(nextTurnModel.getObject());
                        nextTurnModel.setObject(nextTurnModel.getObject() == SquareState.CROSS ? SquareState.CIRCLE : SquareState.CROSS);

                        target.add(XmlReplacementEnablingBehavior.XML, this);
                        target.add(buttons[index]);
                    }
                };
                squareLink.add(new XmlReplacementEnablingBehavior(SVG_NAMESPACE_URI));
                ticTacToe.add(squareLink);
                squareLinks[index] = squareLink;

                var circle = new WebMarkupContainer("circle" + squareId);
                circle.add(AttributeAppender.replace("class", boardStateModel.map(boardState -> boardState[index] == SquareState.CIRCLE ? "circle" : "hidden")));
                squareLink.add(circle);

                var cross = new WebMarkupContainer("cross" + squareId);
                cross.add(AttributeAppender.replace("class", boardStateModel.map(boardState -> boardState[index] == SquareState.CROSS ? "cross" : "hidden")));
                squareLink.add(cross);

                var button = new AjaxButton("button" + squareId)
                {
                    @Override
                    protected void onConfigure()
                    {
                        super.onConfigure();

                        setEnabled(boardStateModel.getObject()[index] == SquareState.EMPTY);
                    }

                    @Override
                    protected void onSubmit(AjaxRequestTarget target)
                    {
                        boardStateModel.getObject()[index] = nextTurnModel.getObject();
                        nextTurnModel.setObject(nextTurnModel.getObject() == SquareState.CROSS ? SquareState.CIRCLE : SquareState.CROSS);

                        target.add(XmlReplacementEnablingBehavior.XML, squareLinks[index]);
                        target.add(this);
                    }
                };
                button.setOutputMarkupId(true);
                svgButtonsForm.add(button);
                buttons[index] = button;
            }
        }

        var resetButton = new AjaxButton("reset")
        {
            @Override
            protected void onSubmit(AjaxRequestTarget target)
            {
                boardStateModel.setObject(newInitialTicTacToeState());
                nextTurnModel.setObject(SquareState.CROSS);

                target.add(XmlReplacementEnablingBehavior.XML, ticTacToe);
                target.add(svgButtonsForm);
            }
        };
        svgButtonsForm.add(resetButton);
    }

    private void addMathmlForm()
    {
        var firstNumberModel = Model.of(1);
        var operatorModel = Model.of(Operator.ADD);
        var secondNumberModel = Model.of(1);
        var outcomeModel = new IModel<>()
        {
            @Override
            public Integer getObject()
            {
                return operatorModel.getObject().compute(
                        firstNumberModel.getObject(),
                        secondNumberModel.getObject());
            }

            @Override
            public void detach()
            {
                firstNumberModel.detach();
                operatorModel.detach();
                secondNumberModel.detach();
            }
        };

        var firstNumberDropDown = new DropDownChoice<>("firstNumberDropDown", firstNumberModel, ONE_TO_NINE);
        var operatorDropDown = new DropDownChoice<>("operatorDropDown", operatorModel, OPERATORS);
        var secondNumberDropDown = new DropDownChoice<>("secondNumberDropDown", secondNumberModel, ONE_TO_NINE);

        var outcome = new Label("outcome", outcomeModel)
                .add(new XmlReplacementEnablingBehavior(MATHML_NAMESPACE_URI));
        var firstNumber = new Label("firstNumber", firstNumberModel)
        {
            @Override
            protected void onInitialize()
            {
                super.onInitialize();

                add(new XmlReplacementEnablingBehavior(MATHML_NAMESPACE_URI));
                var labelThis = this;
                add(new AjaxEventBehavior("click")
                {
                    @Override
                    protected void onEvent(AjaxRequestTarget target)
                    {
                        var currentValue = firstNumberModel.getObject();
                        var newValue = currentValue == 9 ? 1 : currentValue + 1;
                        firstNumberModel.setObject(newValue);
                        target.add(XmlReplacementEnablingBehavior.XML, labelThis, outcome);
                        target.add(firstNumberDropDown);
                    }
                });
            }
        };
        var operator = new Label("operator", operatorModel)
        {
            @Override
            protected void onInitialize()
            {
                super.onInitialize();

                add(new XmlReplacementEnablingBehavior(MATHML_NAMESPACE_URI));
                var labelThis = this;
                add(new AjaxEventBehavior("click")
                {
                    @Override
                    protected void onEvent(AjaxRequestTarget target)
                    {
                        var currentOrdinal = operatorModel.getObject().ordinal();
                        var newOrdinal = currentOrdinal == NUMBER_OF_OPERATORS - 1 ? 0 : currentOrdinal + 1;
                        operatorModel.setObject(Operator.values()[newOrdinal]);
                        target.add(XmlReplacementEnablingBehavior.XML, labelThis, outcome);
                        target.add(operatorDropDown);
                    }
                });
            }
        };
        var secondNumber = new Label("secondNumber", secondNumberModel)
        {
            @Override
            protected void onInitialize()
            {
                super.onInitialize();

                add(new XmlReplacementEnablingBehavior(MATHML_NAMESPACE_URI));
                var labelThis = this;
                add(new AjaxEventBehavior("click")
                {
                    @Override
                    protected void onEvent(AjaxRequestTarget target)
                    {
                        var currentValue = secondNumberModel.getObject();
                        var newValue = currentValue == 9 ? 1 : currentValue + 1;
                        secondNumberModel.setObject(newValue);
                        target.add(XmlReplacementEnablingBehavior.XML, labelThis, outcome);
                        target.add(secondNumberDropDown);
                    }
                });
            }
        };
        add(firstNumber, operator, secondNumber, outcome);

        var form = new Form<>("mathmlForm");
        firstNumberDropDown.setOutputMarkupId(true);
        firstNumberDropDown.add(new AjaxFormComponentUpdatingBehavior("change")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
                target.add(XmlReplacementEnablingBehavior.XML, firstNumber, outcome);
            }
        });
        operatorDropDown.setOutputMarkupId(true);
        operatorDropDown.add(new AjaxFormComponentUpdatingBehavior("change")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
                target.add(XmlReplacementEnablingBehavior.XML, operator, outcome);
            }
        });
        secondNumberDropDown.setOutputMarkupId(true);
        secondNumberDropDown.add(new AjaxFormComponentUpdatingBehavior("change")
        {
            @Override
            protected void onUpdate(AjaxRequestTarget target)
            {
                target.add(XmlReplacementEnablingBehavior.XML, secondNumber, outcome);
            }
        });

        add(
                form.add(
                        firstNumberDropDown,
                        operatorDropDown,
                        secondNumberDropDown
                )
        );
    }

    @Override
    public void renderHead(IHeaderResponse response)
    {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new CssResourceReference(SvgAndMathmlPage.class, "tic-tac-toe.css")));
    }

    private static SquareState[] newInitialTicTacToeState()
    {
        return new SquareState[]
                {
                        SquareState.EMPTY,
                        SquareState.EMPTY,
                        SquareState.EMPTY,
                        SquareState.EMPTY,
                        SquareState.EMPTY,
                        SquareState.EMPTY,
                        SquareState.EMPTY,
                        SquareState.EMPTY,
                        SquareState.EMPTY
                };
    }

    private enum SquareState
    {
        EMPTY,
        CIRCLE,
        CROSS
    }

    private enum Operator
    {
        ADD("+")
        {
            @Override
            int compute(int firstNumber, int secondNubmer)
            {
                return firstNumber + secondNubmer;
            }
        },
        SUBTRACT("-")
        {
            @Override
            int compute(int firstNumber, int secondNubmer)
            {
                return firstNumber - secondNubmer;
            }
        },
        MULTIPLY("×")
        {
            @Override
            int compute(int firstNumber, int secondNubmer)
            {
                return firstNumber * secondNubmer;
            }
        },
        DIVIDE("÷")
        {
            @Override
            int compute(int firstNumber, int secondNubmer)
            {
                return firstNumber / secondNubmer;
            }
        };

        private final String text;

        Operator(String text)
        {
            this.text = text;
        }

        abstract int compute(int firstNumber, int secondNubmer);

        @Override
        public String toString()
        {
            return text;
        }
    }

    private static final List<Operator> OPERATORS = Arrays.asList(Operator.values());
    private static final int NUMBER_OF_OPERATORS = OPERATORS.size();
}
