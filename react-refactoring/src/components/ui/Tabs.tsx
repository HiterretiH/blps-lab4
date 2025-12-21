import React, { useState } from 'react';
import { cn } from '../../utils';

interface TabsProps {
  tabs: { id: string; label: string }[];
  children: React.ReactNode;
  defaultTab?: string;
}

export const Tabs: React.FC<TabsProps> = ({ tabs, children, defaultTab }) => {
  const [activeTab, setActiveTab] = useState(defaultTab || tabs[0]?.id);

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
      <div className="py-4">
        {React.Children.toArray(children).find((child: any) => child.props.id === activeTab)}
      </div>
    </div>
  );
};

interface TabPanelProps {
  id: string;
  children: React.ReactNode;
}

export const TabPanel: React.FC<TabPanelProps> = ({ id, children }) => {
  return <div id={id}>{children}</div>;
};
