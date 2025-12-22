import React, { useState, isValidElement } from 'react';
import { cn } from '../../utils';

interface TabsProps {
  tabs: { id: string; label: string }[];
  children: React.ReactNode;
  defaultTab?: string;
}

interface TabChildProps {
  id: string;
  children?: React.ReactNode;
}

export const Tabs: React.FC<TabsProps> = ({ tabs, children, defaultTab }) => {
  const [activeTab, setActiveTab] = useState(defaultTab || tabs[0]?.id);

  const childrenArray = React.Children.toArray(children);
  const activeChild = childrenArray.find(child => {
    if (isValidElement<TabChildProps>(child)) {
      return child.props.id === activeTab;
    }
    return false;
  });

  return (
    <div>
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          {tabs.map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={cn(
                'border-b-2 px-1 py-4 text-sm font-medium',
                activeTab === tab.id
                  ? 'border-primary-500 text-primary-600'
                  : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700'
              )}
            >
              {tab.label}
            </button>
          ))}
        </nav>
      </div>
      <div className="py-4">{activeChild}</div>
    </div>
  );
};

interface TabPanelProps extends TabChildProps {
  children: React.ReactNode;
}

export const TabPanel: React.FC<TabPanelProps> = ({ id, children }) => {
  return <div id={id}>{children}</div>;
};
