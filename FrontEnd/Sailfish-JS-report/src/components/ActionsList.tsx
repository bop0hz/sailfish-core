/******************************************************************************
 * Copyright 2009-2019 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

import { h, Component } from 'preact';
import Action from '../models/Action';
import '../styles/action.scss';
import { ActionTree } from './ActionTree';
import { StatusType } from '../models/Status';
import AppState from '../state/AppState';
import { connect } from 'preact-redux';
import { selectAction, selectMessages, selectCheckpoint } from '../actions/actionCreators';
import { isCheckpoint } from '../helpers/messageType';

const ACTION_CHECKPOINT_NAME = "GetCheckPoint";

interface ListProps {
    actions: Array<Action>;
    checkpointMessagesIds: Array<number>;
    selectedActionId: number;
    selectedMessageId: number;
    selectedCheckpointId: number;
    actionsFilter: StatusType[];
    filterFields: StatusType[];
    onSelect: (messages: Action) => any;
    onMessageSelect: (id: number, status: StatusType) => any;
    setCheckpointId: (id: number) => any;
}

export class ActionsListBase extends Component<ListProps, {}> {

    private elements: ActionTree[] = [];
    private checkpoints: number[] = [];

    scrollToAction(actionId: number) {
        if (this.elements[actionId]) {
            // smooth behavior is disabled here
            // base - get HTMLElement by ref
            this.elements[actionId].base.scrollIntoView({block: 'center'});
        }    
    }

    componentWillMount() {
        this.checkpoints = this.getAllCheckpoints(this.props.actions);
    }

    componentWillUpdate(nextProps: ListProps) {
        if (nextProps.actions != this.props.actions) {
            this.checkpoints = this.getAllCheckpoints(nextProps.actions);
        }
    }

    shouldComponentUpdate(nextProps: ListProps) {
        if (nextProps.filterFields !== this.props.filterFields) {
            return true;
        }

        if (nextProps.actionsFilter !== this.props.actionsFilter) {
            return true;
        }

        if (nextProps.selectedCheckpointId !== this.props.selectedCheckpointId) {
            return true;
        }

        return nextProps.actions !== this.props.actions ||
            nextProps.selectedActionId !== this.props.selectedActionId ||
            nextProps.selectedMessageId !== this.props.selectedMessageId;
    }

    render({ actions, checkpointMessagesIds, selectedCheckpointId, selectedActionId, selectedMessageId, onSelect, actionsFilter, filterFields, onMessageSelect, setCheckpointId }: ListProps) {

        const cpIndex = checkpointMessagesIds.indexOf(selectedCheckpointId),
            cpEnabled = checkpointMessagesIds.length != 0,
            cpRootClass = [
                "actions-controls-checkpoints",
                cpEnabled ? "" : "disabled"
            ].join(' ');

        return (
            <div class="actions">
                <div class="actions-controls">
                    <div class={cpRootClass}>
                        <div class="actions-controls-checkpoints-icon"/>
                        <div class="actions-controls-checkpoints-title">
                            <p>{cpEnabled ? "" : "No "}Checkpoints</p>
                        </div>
                        <div class="actions-controls-checkpoints-btn prev"
                            onClick={cpEnabled && this.prevCpHandler(checkpointMessagesIds, setCheckpointId, cpIndex)}/>
                        <div class="actions-controls-checkpoints-count">
                            <p>{cpIndex === -1 ? 0 : cpIndex + 1} of {checkpointMessagesIds.length}</p>
                        </div>
                        <div class="actions-controls-checkpoints-btn next"
                            onClick={cpEnabled && this.nextCpHandler(checkpointMessagesIds, setCheckpointId, cpIndex)}/>
                    </div>
                </div>
                <div class="actions-list">
                    {actions.map(action => (
                        <ActionTree 
                            action={action}
                            selectedActionId={selectedActionId}
                            selectedMessageId={selectedMessageId}
                            actionSelectHandler={onSelect}
                            messageSelectHandler={onMessageSelect}
                            actionsFilter={actionsFilter}
                            filterFields={filterFields} 
                            checkpoints={this.checkpoints}
                            checkpointSelectHandler={() => {}}
                            ref={ref => this.elements[action.id] = ref}/>))}
                </div>
            </div> 
        )
    }

    getAllCheckpoints(actions: Action[]) {
        let checkpoints = [];

        actions.forEach(action => {
            checkpoints.push(...this.getActionCheckpoints(action));
        });

        return checkpoints;
    }

    private getActionCheckpoints(action: Action, checkpoints: number[] = []): number[]  {
        if (action.name == ACTION_CHECKPOINT_NAME) {
            return [...checkpoints, action.id];
        }

        action.subNodes.forEach(subNode => {
            if (subNode.actionNodeType == 'action') {
                checkpoints = [...checkpoints, ...this.getActionCheckpoints(subNode as Action, checkpoints)];
            }
        });

        return checkpoints;
    }

    private nextCpHandler = (checkpointMessagesIds: number[], setCheckpointId: (id: number) => any, currentCpIndex: number) => {
        return () => {
            if (currentCpIndex === -1) {
                setCheckpointId(checkpointMessagesIds[0]);
            } else {
                setCheckpointId(checkpointMessagesIds[currentCpIndex + 1] || checkpointMessagesIds[0]);
            }
        }
    }

    private prevCpHandler = (checkpointMessagesIds: number[], setCheckpointId: (id: number) => any, currentCpIndex: number) => {
        return () => {
            if (currentCpIndex === -1) {
                setCheckpointId(checkpointMessagesIds[checkpointMessagesIds.length - 1]);
            } else {
                setCheckpointId(checkpointMessagesIds[currentCpIndex - 1] || checkpointMessagesIds[checkpointMessagesIds.length - 1]);
            }
        }
    }
}   

export const ActionsList = connect((state: AppState) => ({
        actions: state.testCase.actions,
        checkpointMessagesIds: state.testCase.messages.filter(isCheckpoint).map(msg => msg.id),
        selectedActionId: state.selected.actionId,
        selectedMessageId: state.selected.actionId ? null : state.selected.messagesId[0],
        selectedCheckpointId: state.selected.checkpointMessageId,
        actionsFilter: state.actionsFilter,
        filterFields: state.fieldsFilter
    }),
    dispatch => ({
        onSelect: (action: Action) => dispatch(selectAction(action)),
        onMessageSelect: (id: number, status: StatusType) => dispatch(selectMessages([id], status)),
        setCheckpointId: (id: number) => dispatch(selectCheckpoint(id))
    }),
    null,
    {
        withRef: true
    }
)(ActionsListBase);